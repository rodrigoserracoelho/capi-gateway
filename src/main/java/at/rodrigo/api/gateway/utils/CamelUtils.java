package at.rodrigo.api.gateway.utils;

import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.entity.EndpointType;
import at.rodrigo.api.gateway.entity.Path;
import at.rodrigo.api.gateway.processor.AuthProcessor;
import at.rodrigo.api.gateway.processor.PathVariableProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.RouteDefinition;
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

    @Value("${api.gateway.error.endpoint}")
    private String apiGatewayErrorEndpoint;

    @Autowired
    private AuthProcessor authProcessor;

    @Autowired
    private PathVariableProcessor pathProcessor;

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
        String toEndpoint = pathHasParams ? protocol + api.getEndpoint() + Constants.HTTP4_CALL_PARAMS : protocol + api.getEndpoint() + "/" + path.getPath() + Constants.HTTP4_CALL_PARAMS;
        if(pathHasParams) {
            routeDefinition.setHeader(Constants.CAPI_CONTEXT_HEADER, constant(api.getContext()));
        }

        if(api.isSecured()) {
            routeDefinition
                    .streamCaching()
                    .setHeader(Constants.JSON_WEB_KEY_SIGNATURE_ENDPOINT_HEADER, constant(api.getJwsEndpoint()))
                    .setHeader(Constants.BLOCK_IF_IN_ERROR_HEADER, constant(path.isBlockIfInError()))
                    .process(authProcessor)
                    .choice()
                    .when(header(Constants.VALID_HEADER).isEqualTo(true))
                    .process(pathProcessor)
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
                    .process(pathProcessor)
                    .toF(toEndpoint)
                    .end()
                    .setId(routeID);
        }
    }
}
