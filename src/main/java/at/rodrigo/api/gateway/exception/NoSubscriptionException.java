package at.rodrigo.api.gateway.exception;

public class NoSubscriptionException extends Exception {

    public NoSubscriptionException() {
        super("You are not subscribed to this API");
    }
}
