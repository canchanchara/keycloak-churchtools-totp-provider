package org.keycloak.examples.totp.exceptions;

public class TimeProviderException extends RuntimeException {
    public TimeProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
