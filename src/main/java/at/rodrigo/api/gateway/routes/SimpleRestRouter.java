package at.rodrigo.api.gateway.routes;


import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.entity.Path;
import at.rodrigo.api.gateway.processor.AuthProcessor;
import at.rodrigo.api.gateway.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.http.conn.HttpHostConnectException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class SimpleRestRouter extends RouteBuilder {

    @Value("${api.gateway.error.endpoint}")
    private String apiGatewayErrorEndpoint;

    @Value("${api.gateway.rest.endpoint}")
    private String apiGatewayRestEndpoint;

    @Autowired
    private AuthProcessor authProcessor;


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
                        .onException(HttpHostConnectException.class)
                            .setHeader(Constants.REASON_CODE_HEADER, constant(HttpStatus.SERVICE_UNAVAILABLE.value()))
                            .setHeader(Constants.REASON_MESSAGE_HEADER, constant("API NOT AVAILABLE"))
                            .removeHeader(Constants.VALID_HEADER)
                            .continued(true)
                            .toF(Constants.FAIL_REST_ENDPOINT_OBJECT, apiGatewayErrorEndpoint)
                            .removeHeader(Constants.REASON_CODE_HEADER)
                            .removeHeader(Constants.REASON_MESSAGE_HEADER)
                            .end()
                        .setHeader(Constants.JSON_WEB_KEY_SIGNATURE_ENDPOINT_HEADER, constant(api.getJwsEndpoint()))
                        .setHeader(Constants.BLOCK_IF_IN_ERROR_HEADER, constant(path.isBlockIfInError()))
                        .process(authProcessor)

                        .choice()
                        .when(simple("${in.headers.valid} == true"))
                        .toF(Constants.REST_ENDPOINT_OBJECT, (api.getEndpoint() + path.getPath()))
                        .removeHeader(Constants.VALID_HEADER)
                        .log("Response from " + api.getName() + ": ${body}")
                        .convertBodyTo(String.class)

                        .otherwise()
                        .setHeader(Constants.ROUTE_ID_HEADER,constant(Constants.DIRECT_ROUTE_PREFIX + api.getContext() + path.getPath() + "-" + path.getVerb()))
                        .toF(Constants.FAIL_REST_ENDPOINT_OBJECT, apiGatewayErrorEndpoint)
                        .removeHeader(Constants.REASON_CODE_HEADER)
                        .removeHeader(Constants.REASON_MESSAGE_HEADER)
                        .log("ERROR on " + api.getName() + ": ${body}")
                        .convertBodyTo(String.class)
                        .end()
                        .setId(Constants.DIRECT_ROUTE_PREFIX + api.getContext() + path.getPath() + "-" + path.getVerb());
            } else {
                from("direct:" + api.getContext() + path.getPath() + "-" + path.getVerb())
                        .streamCaching()
                        .onException(HttpHostConnectException.class)
                            .setHeader(Constants.REASON_CODE_HEADER, constant(HttpStatus.SERVICE_UNAVAILABLE.value()))
                            .setHeader(Constants.REASON_MESSAGE_HEADER, constant("API NOT AVAILABLE"))
                            .continued(true)
                            .toF(Constants.FAIL_REST_ENDPOINT_OBJECT, apiGatewayErrorEndpoint)
                            .removeHeader(Constants.REASON_CODE_HEADER)
                            .removeHeader(Constants.REASON_MESSAGE_HEADER)
                            .end()
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