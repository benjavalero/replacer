package es.bvalero.replacer.user;

import es.bvalero.replacer.auth.AuthorizationException;
import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.User;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.TestOnly;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebUtils {

    public static final String ACCESS_TOKEN_COOKIE = "access-token";

    // Dependency injection
    private final UserApi userApi;

    public WebUtils(UserApi userApi) {
        this.userApi = userApi;
    }

    public WikipediaLanguage getLanguageFromCookie(HttpServletRequest request) {
        return getUserSession(request).map(UserSession::getLang).orElse(WikipediaLanguage.SPANISH);
    }

    public User getAuthenticatedUser(HttpServletRequest request) {
        UserSession session = getUserSession(request).orElseThrow(() -> {
            LOGGER.warn("Unauthenticated request: access-token cookie not found");
            return new AuthorizationException();
        });
        return userApi
            .findAuthenticatedUser(session.getLang(), session.getAccessToken())
            .orElseThrow(AuthorizationException::new);
    }

    private Optional<UserSession> getUserSession(HttpServletRequest request) {
        Cookie[] requestCookies = request.getCookies();
        if (requestCookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(requestCookies)
            .filter(cookie -> ACCESS_TOKEN_COOKIE.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findAny()
            .map(value -> {
                try {
                    return UserSession.fromCookieValue(value);
                } catch (Exception e) {
                    LOGGER.warn("Unauthenticated request: invalid session cookie");
                    return null;
                }
            });
    }

    /** Build the response cookie after the verification of the user authorization process */
    public static ResponseCookie buildAccessTokenResponseCookie(WikipediaLanguage lang, AccessToken accessToken) {
        return buildAccessTokenResponseCookie(UserSession.of(lang, accessToken));
    }

    private static ResponseCookie buildAccessTokenResponseCookie(UserSession session) {
        // Max age 400 days: https://developer.chrome.com/blog/cookie-max-age-expires/
        // Domain: default
        // SameSite is Lax by default, but it fails in some old browsers, so we set it explicitly.
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, session.toCookieValue())
            .maxAge(400L * 24 * 3600)
            .path("/api")
            .secure(true)
            .httpOnly(true)
            .sameSite("Lax")
            .build();
    }

    public static ResponseCookie buildAccessTokenEmptyCookie() {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, "").maxAge(0).build();
    }

    /** Build a cookie with a user session in order to test the REST controllers */
    @TestOnly
    public static Cookie buildAccessTokenCookie(WikipediaLanguage lang, AccessToken accessToken) {
        return new Cookie(ACCESS_TOKEN_COOKIE, UserSession.of(lang, accessToken).toCookieValue());
    }
}
