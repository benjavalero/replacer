package es.bvalero.replacer.common.resolver;

import es.bvalero.replacer.common.util.WebUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class UserLanguageArgumentResolver implements HandlerMethodArgumentResolver {

    // Dependency injection
    private final WebUtils webUtils;

    public UserLanguageArgumentResolver(WebUtils webUtils) {
        this.webUtils = webUtils;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterAnnotation(UserLanguage.class) != null;
    }

    @Override
    public Object resolveArgument(
        MethodParameter methodParameter,
        @Nullable ModelAndViewContainer modelAndViewContainer,
        NativeWebRequest nativeWebRequest,
        @Nullable WebDataBinderFactory webDataBinderFactory
    ) {
        HttpServletRequest request = (HttpServletRequest) nativeWebRequest.getNativeRequest();
        return webUtils.getLanguageHeader(request);
    }
}
