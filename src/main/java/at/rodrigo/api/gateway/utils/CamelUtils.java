package at.rodrigo.api.gateway.utils;

import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.entity.EndpointType;
import at.rodrigo.api.gateway.entity.Path;
import at.rodrigo.api.gateway.processor.AuthProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Expression;
import org.apache.camel.NoSuchEndpointException;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.model.RouteDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.apache.camel.builder.Builder.simple;
import static org.apache.camel.language.constant.ConstantLanguage.constant;

@Component
@Slf4j
public class CamelUtils {

    @Value("${api.gateway.error.endpoint}")
    private String apiGatewayErrorEndpoint;

    @Autowired
    private AuthProcessor authProcessor;

    public String getCamelHttpEndpoint(Api api) {
        if(api.getEndpointType().equals(EndpointType.HTTP)) {
            return Constants.REST_ENDPOINT_OBJECT;
        } else {
            return Constants.REST_SSL_ENDPOINT_OBJECT;
        }
    }

    public String buildDirectRoute(Api api, Path path) {
        return Constants.DIRECT_ROUTE_IDENTIFIER + api.getContext() + path.getPath() + "-" + path.getVerb();
    }

    public String buildDirectRouteID(Api api, Path path) {
        return Constants.DIRECT_ROUTE_PREFIX + api.getContext() + path.getPath() + "-" + path.getVerb();
    }

    public String buildRestRouteID(Api api, Path path) {
        return Constants.REST_ROUTE_PREFIX + api.getContext() + path.getPath() + "-" + path.getVerb();
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
        String toEndpoint = pathHasParams ? protocol + api.getEndpoint() + Constants.HTTP4_CALL_PARAMS : protocol + api.getEndpoint() + path.getPath() + Constants.HTTP4_CALL_PARAMS;
        buildEndpoint(api.getEndpointType(), api.getEndpoint(), path.getPath(), pathHasParams, routeDefinition);
        if(api.isSecured()) {
            routeDefinition
                    .streamCaching()
                    .setHeader(Constants.JSON_WEB_KEY_SIGNATURE_ENDPOINT_HEADER, constant(api.getJwsEndpoint()))
                    .setHeader(Constants.BLOCK_IF_IN_ERROR_HEADER, constant(path.isBlockIfInError()))
                    .process(authProcessor)
                    .choice()
                    .when(simple("${in.headers.valid} == true"))
                    .toF(toEndpoint)
                    .removeHeader(Constants.VALID_HEADER)
                    .otherwise()
                    .setHeader(Constants.ROUTE_ID_HEADER, constant(routeID))
                    .toF(Constants.FAIL_REST_ENDPOINT_OBJECT, apiGatewayErrorEndpoint)
                    .removeHeader(Constants.REASON_CODE_HEADER)
                    .removeHeader(Constants.REASON_MESSAGE_HEADER)
                    .removeHeader(Constants.ROUTE_ID_HEADER)
                    .end()
                    .setId(routeID);
        } else {
            routeDefinition
                    .streamCaching()
                    .toF(toEndpoint)
                    .end()
                    .setId(routeID);
        }
    }

    private String buildEndpoint(EndpointType endpointType, String endPoint, String path, boolean hasParams, RouteDefinition routeDefinition) {

        String protocol = endpointType.equals(EndpointType.HTTP) ? Constants.HTTP4_PREFIX : Constants.HTTPS4_PREFIX;
        if(hasParams) {
            log.info("-----------------------------------------<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<-------------------------------------");
            log.info(routeDefinition.getEndpointUrl());

            /*List<PropertyDefinition> propertyDefinitions = routeDefinition.getRouteProperties();
            for(PropertyDefinition prop : propertyDefinitions) {
                log.info(prop.getKey() + prop.getValue());
            }*/
            return "";
        } else {
            return protocol + endPoint + path + Constants.HTTP4_CALL_PARAMS;
        }
    }
}
