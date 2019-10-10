package at.rodrigo.api.gateway.routes;

import at.rodrigo.api.gateway.entity.RunningApi;
import at.rodrigo.api.gateway.utils.CamelUtils;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;

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
        camelUtils.buildSuspendedRoute(routeDefinition, runningApi.getRouteId());
    }
}