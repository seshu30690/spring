package me.fullstacker.gateway.constants;

public class GatewayConstants {
	
	private GatewayConstants() {}

	// Regex for acceptable logins
	public static final String LOGIN_REGEX = "^[_'.@A-Za-z0-9-]*$";
	public static final int PASSWORD_MIN_LENGTH = 4;
	public static final int PASSWORD_MAX_LENGTH = 100;
	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String APPROVED = "APPROVED";
	public static final String JWT_TOKEN_EXPIRED_MSG = "Session Token Expired, relogin again";
	// Spring profile used to disable swagger
	public static final String ROLE_USER = "ROLE_USER";
	public static final String PASS_1 = "Password1";
	public static final String PASS_HASH = "$2a$10$JqtfTwIHk8hVqZ5ktnQRnumN72Sq0YT5wCWQH7CUgZgZ9cXnCCFjK";
	public static final String AUTHORITIES_KEY = "auth";
	public static final String PRINCIPAL = "principal";

}
