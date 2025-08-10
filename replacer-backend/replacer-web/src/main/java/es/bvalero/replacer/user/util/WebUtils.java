package es.bvalero.replacer.user.util;

import es.bvalero.replacer.auth.AuthorizationException;
import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.User;
import es.bvalero.replacer.user.UserApi;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.jetbrains.annotations.TestOnly;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class WebUtils {

    public static final String ACCESS_TOKEN_COOKIE = "access-token";

    // Dependency injection
    private final UserApi userApi;

    public WebUtils(UserApi userApi) {
        this.userApi = userApi;
    }

    public WikipediaLanguage getLanguageHeader(HttpServletRequest request) {
        String langHeader = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        return WikipediaLanguage.valueOfCode(langHeader);
    }

    public User getAuthenticatedUser(HttpServletRequest request) {
        // Lang header
        WikipediaLanguage lang = getLanguageHeader(request);

        // Access Token Cookie
        Cookie[] requestCookies = request.getCookies();
        if (requestCookies == null) {
            throw new IllegalArgumentException();
        }
        String accessTokenCookie = Arrays.stream(request.getCookies())
            .filter(cookie -> ACCESS_TOKEN_COOKIE.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findAny()
            .orElseThrow(IllegalArgumentException::new);
        AccessToken accessToken = AccessToken.fromCookieValue(accessTokenCookie);

        return userApi.findAuthenticatedUser(lang, accessToken).orElseThrow(AuthorizationException::new);
    }

    /** Build the response cookie after the verification of the user authorization process */
    public static ResponseCookie buildAccessTokenResponseCookie(AccessToken accessToken) {
        // Max age 400 days: https://developer.chrome.com/blog/cookie-max-age-expires/
        // Domain: default
        // SameSite is Lax by default, but it fails in some old browsers, so we set it explicitly.
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, accessToken.toCookieValue())
            .maxAge((long) 400 * 24 * 3600)
            .path("/api")
            .secure(true)
            .httpOnly(true)
            .sameSite("Lax")
            .build();
    }

    public static ResponseCookie buildAccessTokenEmptyCookie() {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, "").maxAge(0).build();
    }

    /** Build a cookie with an access token in order to test the REST controllers */
    @TestOnly
    public static Cookie buildAccessTokenCookie(AccessToken accessToken) {
        return new Cookie(ACCESS_TOKEN_COOKIE, accessToken.toCookieValue());
    }
}
