package es.bvalero.replacer.common.interceptor;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.User;
import es.bvalero.replacer.user.WebUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MdcInterceptor implements HandlerInterceptor {

    // Dependency injection
    private final WebUtils webUtils;

    public MdcInterceptor(WebUtils webUtils) {
        this.webUtils = webUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // We are only interested in API calls here
        if (!request.getRequestURI().startsWith("/api")) {
            return true;
        }

        // First we suppose the user is already authenticated
        try {
            User user = webUtils.getAuthenticatedUser(request);
            MDC.put("lang", user.getId().getLang().toString());
            MDC.put("user", user.getId().getUsername());
        } catch (Exception e1) {
            try {
                WikipediaLanguage lang = webUtils.getLanguageHeader(request);
                MDC.put("lang", lang.toString());
            } catch (Exception e2) {
                // Do nothing
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        @Nullable Exception ex
    ) {
        MDC.clear();
    }
}
