package at.rodrigo.api.gateway.routes;


import at.rodrigo.api.gateway.cache.RunningApiManager;
import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.entity.Path;
import at.rodrigo.api.gateway.processor.AuthProcessor;
import at.rodrigo.api.gateway.utils.CamelUtils;
import at.rodrigo.api.gateway.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestOperationParamDefinition;
import org.apache.camel.model.rest.RestParamType;
import org.apache.http.conn.HttpHostConnectException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@Slf4j
public class SimpleRestRouter extends RouteBuilder {

    @Value("${api.gateway.error.endpoint}")
    private String apiGatewayErrorEndpoint;

    @Value("${api.gateway.simple.rest.endpoint}")
    private String apiGatewaySimpleRestEndpoint;

    @Autowired
    private AuthProcessor authProcessor;

    @Autowired
    private RunningApiManager runningApiManager;

    @Autowired
    private CamelUtils camelUtils;

    private Api[] apiList;

    @Autowired
    private RestTemplate restTemplate;


    @Override
    public void configure() {

        log.info("Starting configuration of Simple Routes");

        if(apiList == null) {
            apiList = restTemplate.getForObject(apiGatewaySimpleRestEndpoint, Api[].class);
        }

        for(Api api : apiList) {
            try {
                addRoutes(api);
            } catch(Exception e) {
                log.error(e.getMessage(), e);
            }
        }


    }

    public void addRoutes(Api api) throws  Exception {
        for(Path path : api.getPaths()) {
            if(!path.getPath().equals("/error")) {
                RestOperationParamDefinition restParamDefinition = new RestOperationParamDefinition();
                List<String> paramList = camelUtils.evaluatePath(path.getPath());

                String routeID = api.getContext() + path.getPath() + "-" + path.getVerb();
                RouteDefinition routeDefinition;

                switch(path.getVerb()) {
                    case GET:
                        routeDefinition = rest().get("/" + api.getContext() + path.getPath()).route();
                        break;
                    case POST:
                        routeDefinition = rest().post("/" + api.getContext() + path.getPath()).route();
                        break;
                    case PUT:
                        routeDefinition = rest().put("/" + api.getContext() + path.getPath()).route();
                        break;
                    case DELETE:
                        routeDefinition = rest().delete("/" + api.getContext() + path.getPath()).route();
                        break;
                    default:
                        throw new Exception("No verb available");
                }
                camelUtils.buildOnExceptionDefinition(routeDefinition, HttpHostConnectException.class, true, HttpStatus.SERVICE_UNAVAILABLE, "API NOT AVAILABLE", routeID);
                if(paramList.isEmpty()) {
                    camelUtils.buildRoute(routeDefinition, routeID, api, path, false);
                } else {
                    for(String param : paramList) {
                        restParamDefinition.name(param)
                                .type(RestParamType.path)
                                .dataType("String");
                    }
                    camelUtils.buildRoute(routeDefinition, routeID, api, path, true);
                }

            }
        }



        /*for(Path path : api.getPaths()) {
            if(api.isSecured()) {
                from(camelUtils.buildDirectRoute(api, path))
                        .streamCaching()
                        .onException(HttpHostConnectException.class)
                            .setHeader(Constants.REASON_CODE_HEADER, constant(HttpStatus.SERVICE_UNAVAILABLE.value()))
                            .setHeader(Constants.REASON_MESSAGE_HEADER, constant("API NOT AVAILABLE"))
                            .setHeader(Constants.ROUTE_ID_HEADER,constant(api.getContext() + path.getPath() + "-" + path.getVerb()))
                            .removeHeader(Constants.VALID_HEADER)
                            .continued(true)
                            .toF(Constants.FAIL_REST_ENDPOINT_OBJECT, apiGatewayErrorEndpoint)
                            .removeHeader(Constants.REASON_CODE_HEADER)
                            .removeHeader(Constants.REASON_MESSAGE_HEADER)
                            .removeHeader(Constants.ROUTE_ID_HEADER)
                            .end()
                        .setHeader(Constants.JSON_WEB_KEY_SIGNATURE_ENDPOINT_HEADER, constant(api.getJwsEndpoint()))
                        .setHeader(Constants.BLOCK_IF_IN_ERROR_HEADER, constant(path.isBlockIfInError()))
                        .process(authProcessor)

                        .choice()
                        .when(simple("${in.headers.valid} == true"))
                        .toF(camelUtils.getCamelHttpEndpoint(api), (api.getEndpoint() + path.getPath()))
                        .removeHeader(Constants.VALID_HEADER)
                        .log("Response from " + api.getName() + ": ${body}")
                        .convertBodyTo(String.class)

                        .otherwise()
                        .setHeader(Constants.ROUTE_ID_HEADER,constant(api.getContext() + path.getPath() + "-" + path.getVerb()))
                        .toF(Constants.FAIL_REST_ENDPOINT_OBJECT, apiGatewayErrorEndpoint)
                        .removeHeader(Constants.REASON_CODE_HEADER)
                        .removeHeader(Constants.REASON_MESSAGE_HEADER)
                        .removeHeader(Constants.ROUTE_ID_HEADER)
                        .log("ERROR on " + api.getName() + ": ${body}")
                        .convertBodyTo(String.class)
                        .end()
                        .setId(camelUtils.buildDirectRouteID(api, path));

            } else {
                from(camelUtils.buildDirectRoute(api, path))
                        .streamCaching()
                        .onException(HttpHostConnectException.class)
                            .setHeader(Constants.REASON_CODE_HEADER, constant(HttpStatus.SERVICE_UNAVAILABLE.value()))
                            .setHeader(Constants.REASON_MESSAGE_HEADER, constant("API NOT AVAILABLE"))
                            .setHeader(Constants.ROUTE_ID_HEADER,constant(api.getContext() + path.getPath() + "-" + path.getVerb()))
                            .continued(true)
                            .toF(Constants.FAIL_REST_ENDPOINT_OBJECT, apiGatewayErrorEndpoint)
                            .removeHeader(Constants.REASON_CODE_HEADER)
                            .removeHeader(Constants.REASON_MESSAGE_HEADER)
                            .removeHeader(Constants.ROUTE_ID_HEADER)
                            .end()
                        .toF(camelUtils.getCamelHttpEndpoint(api), (api.getEndpoint() + path.getPath()))
                        .log("Response from "+ api.getName() + ": ${body}")
                        .convertBodyTo(String.class)

                        .end()
                        .setId(camelUtils.buildDirectRouteID(api, path));
            }

            runningApiManager.runApi(api.getContext() + path.getPath() + "-" + path.getVerb(), api.getId(), path);

            switch(path.getVerb()) {
                case GET:
                    rest()
                            .get("/" + api.getContext() + path.getPath()).enableCORS(true)
                            .route()
                            .to(camelUtils.buildDirectRoute(api, path))
                            .streamCaching()
                            .end()
                            .marshal().json(JsonLibrary.Jackson)
                            .convertBodyTo(String.class)
                            .end()
                            .setId(camelUtils.buildRestRouteID(api, path));
                    break;
                case POST:
                    rest()
                            .post("/" + api.getContext() + path.getPath()).enableCORS(true)
                            .route()
                            .to(camelUtils.buildDirectRoute(api, path))
                            .streamCaching()
                            .end()
                            .marshal().json(JsonLibrary.Jackson)
                            .convertBodyTo(String.class)
                            .end()
                            .setId(camelUtils.buildRestRouteID(api, path));
                    break;
                case PUT:
                    rest()
                            .put("/" + api.getContext() + path.getPath()).enableCORS(true)
                            .route()
                            .to(camelUtils.buildDirectRoute(api, path))
                            .streamCaching()
                            .end()
                            .marshal().json(JsonLibrary.Jackson)
                            .convertBodyTo(String.class)
                            .end()
                            .setId(camelUtils.buildRestRouteID(api, path));
                    break;
                case DELETE:
                    rest()
                            .delete("/" + api.getContext() + path.getPath()).enableCORS(true)
                            .route()
                            .to(Constants.DIRECT_ROUTE_IDENTIFIER + api.getContext() + path.getPath() + "-" + path.getVerb())
                            .streamCaching()
                            .end()
                            .marshal().json(JsonLibrary.Jackson)
                            .convertBodyTo(String.class)
                            .end()
                            .setId(camelUtils.buildRestRouteID(api, path));
                    break;
                default:
                    log.error("PATH NOT AVAILABLE: {}", path.getVerb());
                    break;
            }
        }*/
    }
}