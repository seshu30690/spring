package me.fullstacker.gateway.security.jwt;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.util.WebUtils;

/**
 * 
 * @author Seshu Kandimalla
 *
 */
public class HttpCookie {
	
	private HttpCookie() {
		
	}

	public static void create(HttpServletResponse httpServletResponse, String name, String value, Integer maxAge
			) {
		Cookie cookie = new Cookie(name, value);
		cookie.setSecure(false);
		cookie.setHttpOnly(true);
		cookie.setMaxAge(maxAge);
		cookie.setPath("/");
		httpServletResponse.addCookie(cookie);
	}

	public static void clear(HttpServletResponse httpServletResponse, String name) {
		Cookie cookie = new Cookie(name, null);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setMaxAge(0);
		httpServletResponse.addCookie(cookie);
	}

	public static String getValue(HttpServletRequest httpServletRequest, String name) {
		Cookie cookie = WebUtils.getCookie(httpServletRequest, name);
		return cookie != null ? cookie.getValue() : null;
	}

}
