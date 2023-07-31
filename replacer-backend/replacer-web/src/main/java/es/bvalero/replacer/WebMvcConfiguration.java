package es.bvalero.replacer;

import es.bvalero.replacer.user.AuthenticatedUserArgumentResolver;
import es.bvalero.replacer.user.UserLanguageArgumentResolver;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * This configuration will be applied in order to perform calls
 * from frontend without getting CORS warnings from certain browsers.
 */
@Configuration
public class WebMvcConfiguration {

    @Value("${replacer.cors.allowed.origins}")
    private String corsAllowedOrigins;

    @Autowired
    private AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;

    @Autowired
    private UserLanguageArgumentResolver userLanguageArgumentResolver;

    @Autowired
    private MdcInterceptor mdcInterceptor;

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
