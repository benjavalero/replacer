package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Arrays;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthenticatedUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private UserService userService;

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterAnnotation(AuthenticatedUser.class) != null;
    }

    @SneakyThrows
    @Override
    public Object resolveArgument(
        MethodParameter methodParameter,
        ModelAndViewContainer modelAndViewContainer,
        NativeWebRequest nativeWebRequest,
        WebDataBinderFactory webDataBinderFactory
    ) {
        HttpServletRequest request = (HttpServletRequest) nativeWebRequest.getNativeRequest();

        // Lang header
        String langHeader = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        WikipediaLanguage lang = WikipediaLanguage.valueOfCode(langHeader);

        // Access Token Cookie
        try {
            String accessTokenCookie = Arrays
                .stream(request.getCookies())
                .filter(cookie -> AccessToken.COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
            AccessToken accessToken = AccessToken.fromCookieValue(accessTokenCookie);

            return userService.findAuthenticatedUser(lang, accessToken).orElseThrow(IllegalArgumentException::new);
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
    }
}
