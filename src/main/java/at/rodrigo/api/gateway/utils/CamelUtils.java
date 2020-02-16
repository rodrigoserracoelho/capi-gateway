package at.rodrigo.api.gateway.utils;

import at.rodrigo.api.gateway.cache.RunningApiManager;
import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.entity.EndpointType;
import at.rodrigo.api.gateway.entity.Path;
import at.rodrigo.api.gateway.entity.RunningApi;
import at.rodrigo.api.gateway.processor.AuthProcessor;
import at.rodrigo.api.gateway.processor.MetricsProcessor;
import at.rodrigo.api.gateway.processor.PathVariableProcessor;
import at.rodrigo.api.gateway.routes.DynamicPathRouteBuilder;
import at.rodrigo.api.gateway.routes.SuspendedRouteBuilder;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.zipkin.ZipkinTracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.apache.camel.builder.Builder.header;
import static org.apache.camel.language.constant.ConstantLanguage.constant;


@Component
@Slf4j
public class CamelUtils {

    @Value("${api.gateway.traffic.inspector.enabled}")
    private boolean trafficInspectorEnabled;

    @Value("${api.gateway.traffic.inspector.kafka.topic}")
    private String trafficInspectorKafkaTopic;

    @Value("${api.gateway.traffic.inspector.kafka.broker}")
    private String trafficInspectorKafkaBroker;

    @Value("${api.gateway.error.endpoint}")
    private String apiGatewayErrorEndpoint;

    @Autowired
    private AuthProcessor authProcessor;

    @Autowired
    private PathVariableProcessor pathProcessor;

    @Autowired
    private MetricsProcessor metricsProcessor;

    @Autowired
    private CompositeMeterRegistry meterRegistry;

    @Autowired
    private ZipkinTracer zipkinTracer;

    @Autowired
    private RunningApiManager runningApiManager;

    @Autowired
    private CamelContext camelContext;

    private void registerMetric(String routeID) {
        meterRegistry.counter(routeID);
    }

    public List<String> evaluatePath(String fullPath) {
        List<String> paramList = new ArrayList<>();
        if(fullPath.contains("{")) {
            String[] splittedPath = fullPath.split("/");
            for(String path : splittedPath) {
                if(path.contains("{")) {
                    String name = path.substring(1, path.length()-1);
                    paramList.add(name);
                }
            }
        }
        return paramList;
    }

    public void buildOnExceptionDefinition(RouteDefinition routeDefinition, Class exceptionClass, boolean continued, HttpStatus httpStatus, String message, String routeID) {
        routeDefinition
                .onException(exceptionClass)
                .continued(continued)
                .setHeader(Constants.REASON_CODE_HEADER, constant(httpStatus.value()))
                .setHeader(Constants.REASON_MESSAGE_HEADER, constant(message))
                .setHeader(Constants.ROUTE_ID_HEADER, constant(routeID))
                .toF(Constants.FAIL_REST_ENDPOINT_OBJECT, apiGatewayErrorEndpoint)
                .removeHeader(Constants.REASON_CODE_HEADER)
                .removeHeader(Constants.REASON_MESSAGE_HEADER)
                .removeHeader(Constants.ROUTE_ID_HEADER)
                .end();
    }

    public void buildRoute(RouteDefinition routeDefinition, String routeID, Api api, Path path, boolean pathHasParams) {
        String protocol = api.getEndpointType().equals(EndpointType.HTTP) ? Constants.HTTP4_PREFIX : Constants.HTTPS4_PREFIX;
        String toEndpoint = pathHasParams ? protocol + api.getEndpoint() + Constants.HTTP4_CALL_PARAMS : protocol + api.getEndpoint() + "/" + path.getPath() + Constants.HTTP4_CALL_PARAMS;
        if(pathHasParams) {
            routeDefinition.setHeader(Constants.CAPI_CONTEXT_HEADER, constant(api.getContext()));
        }
        if(trafficInspectorEnabled) {
            routeDefinition
                    .setBody(constant(routeID))
                    .to("kafka:" + trafficInspectorKafkaTopic + "?brokers=" + trafficInspectorKafkaBroker);
        }
        if(api.isSecured()) {
            routeDefinition
                    .streamCaching()
                    .setHeader(Constants.BLOCK_IF_IN_ERROR_HEADER, constant(api.isBlockIfInError()))
                    .setHeader(Constants.API_ID_HEADER, constant(api.getId()))
                    .process(authProcessor)
                    .choice()
                    .when(header(Constants.VALID_HEADER).isEqualTo(true))
                    .process(pathProcessor)
                    .toF(toEndpoint)
                    .removeHeader(Constants.VALID_HEADER)
                    .process(metricsProcessor)
                    .otherwise()
                    .setHeader(Constants.ROUTE_ID_HEADER, constant(routeID))
                    .toF(Constants.FAIL_REST_ENDPOINT_OBJECT, apiGatewayErrorEndpoint)
                    .removeHeader(Constants.REASON_CODE_HEADER)
                    .removeHeader(Constants.REASON_MESSAGE_HEADER)
                    .removeHeader(Constants.ROUTE_ID_HEADER)
                    .process(metricsProcessor)
                    .end()
                    .setId(routeID);
        } else {

            routeDefinition
                    .streamCaching()
                    .process(pathProcessor)
                    .toF(toEndpoint)
                    .process(metricsProcessor)
                    .end()
                    .setId(routeID);
        }

        registerMetric(normalizeRouteId(api, path));
        zipkinTracer.addServerServiceMapping(api.getContext() + path.getPath(), normalizeRouteId(api, path));
        runningApiManager.runApi(routeID, api, path.getPath(), path.getVerb());

    }

    public void buildRoute(RouteDefinition routeDefinition, String routeID, RunningApi runningApi, boolean pathHasParams) {
        String protocol = runningApi.getEndpointType().equals(EndpointType.HTTP) ? Constants.HTTP4_PREFIX : Constants.HTTPS4_PREFIX;
        String toEndpoint = pathHasParams ? protocol + runningApi.getEndpoint() + Constants.HTTP4_CALL_PARAMS : protocol + runningApi.getEndpoint() + "/" + runningApi.getPath() + Constants.HTTP4_CALL_PARAMS;
        if(pathHasParams) {
            routeDefinition.setHeader(Constants.CAPI_CONTEXT_HEADER, constant(runningApi.getContext()));
        }
        if(trafficInspectorEnabled) {
            routeDefinition
                    .setBody(constant(routeID))
                    .to("kafka:" + trafficInspectorKafkaTopic + "?brokers=" + trafficInspectorKafkaBroker);
        }
        if(runningApi.isSecured()) {
            routeDefinition
                    .streamCaching()
                    .setHeader(Constants.BLOCK_IF_IN_ERROR_HEADER, constant(runningApi.isBlockIfInError()))
                    .setHeader(Constants.API_ID_HEADER, constant(runningApi.getId()))
                    .process(authProcessor)
                    .choice()
                    .when(header(Constants.VALID_HEADER).isEqualTo(true))
                    .process(pathProcessor)
                    .toF(toEndpoint)
                    .removeHeader(Constants.VALID_HEADER)
                    .process(metricsProcessor)
                    .otherwise()
                    .setHeader(Constants.ROUTE_ID_HEADER, constant(routeID))
                    .toF(Constants.FAIL_REST_ENDPOINT_OBJECT, apiGatewayErrorEndpoint)
                    .removeHeader(Constants.REASON_CODE_HEADER)
                    .removeHeader(Constants.REASON_MESSAGE_HEADER)
                    .removeHeader(Constants.ROUTE_ID_HEADER)
                    .process(metricsProcessor)
                    .end()
                    .setId(routeID);
        } else {
            routeDefinition
                    .streamCaching()
                    .process(pathProcessor)
                    .toF(toEndpoint)
                    .process(metricsProcessor)
                    .end()
                    .setId(routeID);
        }
        registerMetric(runningApi.getRouteId());
        zipkinTracer.addServerServiceMapping(runningApi.getContext() + runningApi.getPath(), routeID);
    }

    public void buildSuspendedRoute(RouteDefinition routeDefinition, String routeID) {
        routeDefinition
                .setHeader(Constants.ROUTE_ID_HEADER, constant(routeID))
                .toF(Constants.FAIL_REST_ENDPOINT_OBJECT, apiGatewayErrorEndpoint)
                .end()
                .setId(routeID);
    }

    public String normalizeRouteId(Api api, Path path) {
        return (api.getContext() + path.getPath() + "_" + path.getVerb()).replaceAll("/", "_").replaceAll("-", "_").replaceAll("[{}]", "");
    }

    public String normalizeRouteId(String route) {
        return route.replaceAll("/", "_").replaceAll("-", "_").replaceAll("[{}]", "");
    }

    public void suspendRoute(RunningApi runningApi) {
        Route routeToRemove = camelContext.getRoute(runningApi.getRouteId());
        if(routeToRemove != null) {
            try {
                log.info("Removing route: {}", routeToRemove.getId());
                camelContext.getRouteController().stopRoute(runningApi.getRouteId());
                camelContext.removeRoute(runningApi.getRouteId());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else {
            log.info("Route does not exist: {}" , runningApi.getRouteId());
        }
    }

    public void addSuspendedRoute(RunningApi runningApi) {
        try {
            log.info("Add suspended route: {}", runningApi.getRouteId());
            camelContext.addRoutes(new SuspendedRouteBuilder(camelContext, this, runningApi));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void addActiveRoute(RunningApi runningApi) {
        try {
            log.info("Add route: {}", runningApi.getRouteId());
            camelContext.addRoutes(new DynamicPathRouteBuilder(camelContext, this, runningApi));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
