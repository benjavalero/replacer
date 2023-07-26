package es.bvalero.replacer;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.util.WebUtils;
import es.bvalero.replacer.user.User;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MdcInterceptor implements HandlerInterceptor {

    @Autowired
    private WebUtils webUtils;

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
        } catch (Exception e) {
            WikipediaLanguage lang = webUtils.getLanguageHeader(request);
            MDC.put("lang", lang.toString());
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
