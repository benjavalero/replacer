package es.bvalero.replacer.common.util;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.AccessToken;
import es.bvalero.replacer.user.AuthorizationException;
import es.bvalero.replacer.user.User;
import es.bvalero.replacer.user.UserService;
import java.util.Arrays;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class WebUtils {

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
            .filter(cookie -> AccessToken.COOKIE_NAME.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findAny()
            .orElseThrow(IllegalArgumentException::new);
        AccessToken accessToken = AccessToken.fromCookieValue(accessTokenCookie);

        return userService.findAuthenticatedUser(lang, accessToken).orElseThrow(AuthorizationException::new);
    }
}
