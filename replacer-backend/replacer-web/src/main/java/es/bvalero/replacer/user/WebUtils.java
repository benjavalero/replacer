package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Arrays;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class WebUtils {

    public static final String ACCESS_TOKEN_COOKIE = "access-token";

    // Dependency injection
    private final UserService userService;

    public WebUtils(UserService userService) {
        this.userService = userService;
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
        String accessTokenCookie = Arrays
            .stream(request.getCookies())
            .filter(cookie -> ACCESS_TOKEN_COOKIE.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findAny()
            .orElseThrow(IllegalArgumentException::new);
        AccessToken accessToken = AccessToken.fromCookieValue(accessTokenCookie);

        return userService.findAuthenticatedUser(lang, accessToken).orElseThrow(AuthorizationException::new);
    }

    public static Cookie buildAccessTokenCookie(AccessToken accessToken) {
        return new Cookie(WebUtils.ACCESS_TOKEN_COOKIE, accessToken.toCookieValue());
    }
}
