package at.rodrigo.api.gateway.utils;

import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.entity.EndpointType;
import at.rodrigo.api.gateway.entity.Path;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Iterator;

@Component
@Slf4j
public class CamelUtils {

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

    public void testSwagg() {



    }
}
