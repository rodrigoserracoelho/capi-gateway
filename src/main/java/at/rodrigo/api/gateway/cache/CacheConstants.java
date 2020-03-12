package at.rodrigo.api.gateway.cache;

public class CacheConstants {

    private CacheConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String RUNNING_API_IMAP_NAME = "runningApi";
    public static final String API_IMAP_NAME = "api";
    public static final String THROTTLING_POLICIES_IMAP_NAME = "throttlingPolicies";



}
