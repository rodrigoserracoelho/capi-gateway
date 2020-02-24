package at.rodrigo.api.gateway.exception;

public class InvalidTokenException extends Exception {

    public InvalidTokenException() {
        super("Invalid token was provided");
    }
}
