package at.rodrigo.api.gateway.routes;


import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.entity.Path;
import at.rodrigo.api.gateway.processor.AuthProcessor;
import at.rodrigo.api.gateway.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;

@Component
@Slf4j
public class SimpleRestRouter extends RouteBuilder {

    @Value("${api.gateway.error.endpoint}")
    private String apiGatewayErrorEndpoint;

    @Value("${api.gateway.rest.endpoint}")
    private String apiGatewayRestEndpoint;

    @Autowired
    private AuthProcessor processor;


    @Override
    public void configure() {

        log.info("Starting configuration of Dynamic Routes");

        RestTemplate restTemplate = new RestTemplate();
        Api[] apiList = restTemplate.getForObject(apiGatewayRestEndpoint, Api[].class);

        for(Api api : apiList) {
            addRoute(api);
        }
    }

    public void addRoute(Api api) {
        for(Path path : api.getPaths()) {
            if(api.isSecured()) {
                from("direct:" + api.getContext() + path.getPath() + "-" + path.getVerb())
                        .streamCaching()
                        .setHeader(Constants.JSON_WEB_KEY_SIGNATURE_ENDPOINT_HEADER, constant(api.getJwsEndpoint()))
                        .setHeader(Constants.BLOCK_IF_IN_ERROR_HEADER, constant(path.isBlockIfInError()))
                        .process(processor)

                        .choice()
                        .when(simple("${in.headers.VALID} == true"))
                        .toF(Constants.REST_ENDPOINT_OBJECT, (api.getEndpoint() + path.getPath()))
                        .log("Response from " + api.getName() + ": ${body}")
                        .convertBodyTo(String.class)

                        .otherwise()
                        .setHeader("routeId",constant(Constants.DIRECT_ROUTE_PREFIX + api.getContext() + path.getPath() + "-" + path.getVerb()))
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