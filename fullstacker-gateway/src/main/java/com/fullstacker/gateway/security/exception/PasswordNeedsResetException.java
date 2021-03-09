package me.fullstacker.gateway.security.exception;

import org.springframework.security.authentication.AccountStatusException;

/**
 * 
 * @author Seshu Kandimalla
 *
 */
public class PasswordNeedsResetException extends AccountStatusException {

	private static final long serialVersionUID = 3938163753810533931L;

	/**
	 * Constructs a <code>NotPermittedException</code> with the specified message.
	 *
	 * @param msg
	 *            the detail message.
	 */
	public PasswordNeedsResetException(String msg) {
		super(msg);
	}

	/**
	 * Constructs a <code>NotPermittedException</code> with the specified message
	 * and root cause.
	 *
	 * @param msg
	 *            the detail message.
	 * @param t
	 *            root cause
	 */
	public PasswordNeedsResetException(String msg, Throwable t) {
		super(msg, t);
	}

}
