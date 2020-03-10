package at.rodrigo.api.gateway.routes;

import at.rodrigo.api.gateway.entity.RunningApi;
import at.rodrigo.api.gateway.utils.CamelUtils;
import at.rodrigo.api.gateway.utils.Constants;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.rest.RestOperationParamDefinition;
import org.apache.camel.model.rest.RestParamType;

import java.util.List;

public class PathRouteRepublisher extends RouteBuilder {

    private RunningApi runningApi;

    private CamelUtils camelUtils;

    public PathRouteRepublisher(CamelContext context, CamelUtils camelUtils, RunningApi runningApi) {
        super(context);
        this.runningApi = runningApi;
        this.camelUtils = camelUtils;
    }

    @Override
    public void configure() {
        log.info("Starting to republish a previously blocked route");

        try {
            addRoute(runningApi);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    private void addRoute(RunningApi runningApi) throws  Exception {
        RestOperationParamDefinition restParamDefinition = new RestOperationParamDefinition();
        List<String> paramList = camelUtils.evaluatePath(runningApi.getPath());
        RouteDefinition routeDefinition;
        switch(runningApi.getVerb()) {
            case GET:
                routeDefinition = rest().get("/" + runningApi.getContext() + runningApi.getPath()).route();
                break;
            case POST:
                routeDefinition = rest().post("/" + runningApi.getContext() + runningApi.getPath()).route().setProperty(Constants.REST_CALL_BODY, body());
                break;
            case PUT:
                routeDefinition = rest().put("/" + runningApi.getContext() + runningApi.getPath()).route().setProperty(Constants.REST_CALL_BODY, body());
                break;
            case DELETE:
                routeDefinition = rest().delete("/" + runningApi.getContext() + runningApi.getPath()).route();
                break;
            default:
                throw new Exception("No verb available");
        }
        camelUtils.buildOnExceptionDefinition(routeDefinition, runningApi.isZipkinTraceIdVisible(), runningApi.isInternalExceptionMessageVisible(), runningApi.isInternalExceptionVisible(), runningApi.getRouteId());
        if(paramList.isEmpty()) {
            camelUtils.buildRoute(routeDefinition, runningApi.getRouteId(), runningApi, false);
        } else {
            for(String param : paramList) {
                restParamDefinition.name(param)
                        .type(RestParamType.path)
                        .dataType("String");
            }
            camelUtils.buildRoute(routeDefinition, runningApi.getRouteId(), runningApi, true);
        }
    }
}