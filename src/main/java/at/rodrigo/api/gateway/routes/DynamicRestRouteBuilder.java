package at.rodrigo.api.gateway.routes;

import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.entity.Path;
import at.rodrigo.api.gateway.processor.AuthProcessor;
import at.rodrigo.api.gateway.utils.Constants;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

public class DynamicRestRouteBuilder extends RouteBuilder {

    private Api api;
    private AuthProcessor authProcessor;
    private String apiGatewayErrorEndpoint;

    public DynamicRestRouteBuilder(CamelContext context, AuthProcessor authProcessor, String apiGatewayErrorEndpoint, Api api) {
        super(context);
        this.api = api;
        this.authProcessor = authProcessor;
        this.apiGatewayErrorEndpoint = apiGatewayErrorEndpoint;
    }

    @Override
    public void configure() throws Exception {
        for(Path path : api.getPaths()) {
            if(api.isSecured()) {
                from("direct:" + api.getContext() + path.getPath() + "-" + path.getVerb())
                        .streamCaching()
                        .process(authProcessor)
                        .choice()
                        .when(simple("${in.headers.VALID} == true"))
                        .toF(Constants.REST_ENDPOINT_OBJECT, (api.getEndpoint() + path.getPath()))
                        .log("Response from " + api.getName() + ": ${body}")
                        .convertBodyTo(String.class)

                        .otherwise()
                        .toF(Constants.FAIL_REST_ENDPOINT_OBJECT, apiGatewayErrorEndpoint)
                        .log("ERROR on " + api.getName() + ": ${body}")
                        .convertBodyTo(String.class)

                        .end()
                        .setId(Constants.DIRECT_ROUTE_PREFIX + api.getContext() + path.getPath() + "-" + path.getVerb());
            } else {
                from("direct:" + api.getContext() + path.getPath() + "-" + path.getVerb())
                        .streamCaching()
                        .toF(Constants.REST_ENDPOINT_OBJECT, (api.getEndpoint() + path.getPath()))
                        .log("Response from "+ api.getName() + ": ${body}")
                        .convertBodyTo(String.class)

                        .end()
                        .setId(Constants.DIRECT_ROUTE_PREFIX + api.getContext() + path.getPath() + "-" + path.getVerb());
            }

            switch(path.getVerb()) {
                case GET:
                    rest()
                            .get("/" + api.getContext() + path.getPath()).enableCORS(true)
                            .route()
                            .to("direct:" + api.getContext() + path.getPath() + "-" + path.getVerb())
                            .streamCaching()
                            .end()
                            .marshal().json(JsonLibrary.Jackson)
                            .convertBodyTo(String.class)
                            .end()
                            .setId(Constants.REST_ROUTE_PREFIX + api.getContext() + path.getPath() + "-" + path.getVerb());
                    break;
                case POST:
                    rest()
                            .post("/" + api.getContext() + path.getPath()).enableCORS(true)
                            .route()
                            .to("direct:" + api.getContext() + path.getPath() + "-" + path.getVerb())
                            .streamCaching()
                            .end()
                            .marshal().json(JsonLibrary.Jackson)
                            .convertBodyTo(String.class)
                            .end()
                            .setId(Constants.REST_ROUTE_PREFIX + api.getContext() + path.getPath() + "-" + path.getVerb());
                    break;
                case PUT:
                    rest()
                            .put("/" + api.getContext() + path.getPath()).enableCORS(true)
                            .route()
                            .to("direct:" + api.getContext() + path.getPath() + "-" + path.getVerb())
                            .streamCaching()
                            .end()
                            .marshal().json(JsonLibrary.Jackson)
                            .convertBodyTo(String.class)
                            .end()
                            .setId(Constants.REST_ROUTE_PREFIX + api.getContext() + path.getPath() + "-" + path.getVerb());
                    break;
            }
        }

    }
}
