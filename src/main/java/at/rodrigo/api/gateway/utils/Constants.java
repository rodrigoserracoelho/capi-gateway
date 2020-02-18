package at.rodrigo.api.gateway.utils;

public class Constants {

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String APPLICATION_NAME = "CAPI";
    public static final String FAIL_REST_ENDPOINT_OBJECT = "https:%s?throwExceptionOnFailure=false&connectTimeout=1000&bridgeEndpoint=true&copyHeaders=true&connectionClose=true";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BLOCK_IF_IN_ERROR_HEADER = "block-if-in-error";
    public static final String VALID_HEADER = "valid";
    public static final String REASON_CODE_HEADER = "error-reason-code";
    public static final String REASON_MESSAGE_HEADER = "error-reason-message";
    public static final String ROUTE_ID_HEADER = "routeID";
    public static final String ERROR = "error";
    public static final String RESULT = "result";
    public static final String API = "api";
    public static final String HTTP4_PREFIX = "http://";
    public static final String HTTPS4_PREFIX = "https://";
    public static final String HTTP4_CALL_PARAMS = "?throwExceptionOnFailure=false&bridgeEndpoint=true&copyHeaders=true&connectionClose=true";
    public static final String CAPI_CONTEXT_HEADER = "CapiContext";
    public static final String API_ID_HEADER = "api_id";
}
