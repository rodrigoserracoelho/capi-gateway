package at.rodrigo.api.gateway.routes;

import at.rodrigo.api.gateway.entity.RunningApi;
import at.rodrigo.api.gateway.utils.CamelUtils;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.rest.RestOperationParamDefinition;
import org.apache.camel.model.rest.RestParamType;

import java.util.List;

public class SuspendedRouteBuilder extends RouteBuilder {

    private RunningApi runningApi;

    private CamelUtils camelUtils;

    public SuspendedRouteBuilder(CamelContext context, CamelUtils camelUtils, RunningApi runningApi) {
        super(context);
        this.runningApi = runningApi;
        this.camelUtils = camelUtils;
    }

    @Override
    public void configure() {
        log.info("Starting to publish a suspended route");

        try {
            addRoute(runningApi);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    private void addRoute(RunningApi runningApi) throws  Exception {
        RestOperationParamDefinition restParamDefinition = new RestOperationParamDefinition();
        RouteDefinition routeDefinition;
        switch(runningApi.getVerb()) {
            case GET:
                routeDefinition = rest().get("/" + runningApi.getContext() + runningApi.getPath()).route();
                break;
            case POST:
                routeDefinition = rest().post("/" + runningApi.getContext() + runningApi.getPath()).route();
                break;
            case PUT:
                routeDefinition = rest().put("/" + runningApi.getContext() + runningApi.getPath()).route();
                break;
            case DELETE:
                routeDefinition = rest().delete("/" + runningApi.getContext() + runningApi.getPath()).route();
                break;
            default:
                throw new Exception("No verb available");
        }
        List<String> paramList = camelUtils.evaluatePath(runningApi.getPath());
        if(!paramList.isEmpty()) {
            for(String param : paramList) {
                restParamDefinition.name(param)
                        .type(RestParamType.path)
                        .dataType("String");
            }
            camelUtils.buildSuspendedRoute(routeDefinition, runningApi, true);
        }
        camelUtils.buildSuspendedRoute(routeDefinition, runningApi, false);
    }
}