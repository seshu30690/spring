package me.fullstacker.gateway.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * This exception is thrown in case of a not activated user trying to authenticate.
 */
public class UnsupportedAuthProviderException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    public UnsupportedAuthProviderException(String message) {
        super(message);
    }

    public UnsupportedAuthProviderException(String message, Throwable t) {
        super(message, t);
    }
}
