package at.rodrigo.api.gateway.routes;

import at.rodrigo.api.gateway.cache.RunningApiManager;
import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.entity.Path;
import at.rodrigo.api.gateway.processor.AuthProcessor;
import at.rodrigo.api.gateway.utils.CamelUtils;
import at.rodrigo.api.gateway.utils.GrafanaUtils;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.rest.RestOperationParamDefinition;
import org.apache.camel.model.rest.RestParamType;
import org.apache.http.conn.HttpHostConnectException;
import org.springframework.http.HttpStatus;

import java.net.UnknownHostException;
import java.util.List;

public class DynamicSwaggerRouteBuilder extends RouteBuilder {

    private Api api;
    private AuthProcessor authProcessor;
    private String apiGatewayErrorEndpoint;

    private RunningApiManager runningApiManager;

    private CamelUtils camelUtils;

    private GrafanaUtils grafanaUtils;

    public DynamicSwaggerRouteBuilder(CamelContext context, AuthProcessor authProcessor, RunningApiManager runningApiManager, CamelUtils camelUtils, GrafanaUtils grafanaUtils, String apiGatewayErrorEndpoint, Api api) {
        super(context);
        this.api = api;
        this.authProcessor = authProcessor;
        this.runningApiManager = runningApiManager;
        this.apiGatewayErrorEndpoint = apiGatewayErrorEndpoint;
        this.camelUtils = camelUtils;
        this.grafanaUtils = grafanaUtils;
    }

    @Override
    public void configure() {
        try {
            addRoutes(api);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void addRoutes(Api api) throws Exception {
        for(Path path : api.getPaths()) {
            if(!path.getPath().equals("/error")) {
                RestOperationParamDefinition restParamDefinition = new RestOperationParamDefinition();
                List<String> paramList = camelUtils.evaluatePath(path.getPath());

                String routeID = camelUtils.normalizeRouteId(api, path);
                path.setRouteID(routeID);
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
                camelUtils.buildOnExceptionDefinition(routeDefinition, UnknownHostException.class, true, HttpStatus.SERVICE_UNAVAILABLE, "API ENDPOINT WITH WRONG HOST", routeID);
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
        grafanaUtils.addToGrafana(api);
    }
}