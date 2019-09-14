package at.rodrigo.api.gateway.utils;

public class Constants {

    public static final String DIRECT_ROUTE_PREFIX = "DIRECT-";
    public static final String REST_ROUTE_PREFIX = "REST-";
    public static final String REST_ENDPOINT_OBJECT = "http4:%s?connectTimeout=1000&bridgeEndpoint=true&copyHeaders=true&connectionClose=true";
    public static final String FAIL_REST_ENDPOINT_OBJECT = "http4:%s?throwExceptionOnFailure=false&connectTimeout=1000&bridgeEndpoint=true&copyHeaders=true&connectionClose=true";
    public static final String JSON_WEB_KEY_SIGNATURE_ENDPOINT_HEADER = "jwks_endpoint";
    public static final String AUTHORIZATION_HEADER = "Authorization";
}
