package es.bvalero.replacer;

import es.bvalero.replacer.common.interceptor.MdcInterceptor;
import es.bvalero.replacer.common.resolver.AuthenticatedUserArgumentResolver;
import es.bvalero.replacer.common.resolver.UserLanguageArgumentResolver;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * This configuration will be applied in order to perform calls
 * from frontend without getting CORS warnings from certain browsers.
 */
@Configuration
public class WebMvcConfiguration {

    @Value("${replacer.cors.allowed.origins}")
    private String corsAllowedOrigins;

    // Dependency injection
    private final AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;
    private final UserLanguageArgumentResolver userLanguageArgumentResolver;
    private final MdcInterceptor mdcInterceptor;

    public WebMvcConfiguration(
        AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver,
        UserLanguageArgumentResolver userLanguageArgumentResolver,
        MdcInterceptor mdcInterceptor
    ) {
        this.authenticatedUserArgumentResolver = authenticatedUserArgumentResolver;
        this.userLanguageArgumentResolver = userLanguageArgumentResolver;
        this.mdcInterceptor = mdcInterceptor;
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Profile("backend")
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry
                    .addMapping("/api/**")
                    .allowCredentials(true)
                    .allowedOrigins(corsAllowedOrigins)
                    .exposedHeaders("X-Pagination-Total-Pages")
                    .allowedMethods("GET", "POST");
            }

            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
                argumentResolvers.add(authenticatedUserArgumentResolver);
                argumentResolvers.add(userLanguageArgumentResolver);
            }

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(mdcInterceptor);
            }
        };
    }
}
