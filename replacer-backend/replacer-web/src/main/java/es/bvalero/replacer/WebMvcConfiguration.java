package es.bvalero.replacer;

import es.bvalero.replacer.common.util.AuthenticatedUserArgumentResolver;
import es.bvalero.replacer.common.util.UserLanguageArgumentResolver;
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

            // https://github.com/FraktonDevelopers/spring-boot-angular-maven-build
            // In Angular by default all paths are supported and accessible but Spring Boot tries to manage paths by itself.
            // The /** pattern is matched by AntPathMatcher to directories in the path,
            // so the configuration will be applied to our project routes.
            // Also, the PathResourceResolver will try to find any resource under the location given,
            // so all the requests that are not handled by Spring Boot will be redirected to static/index.html
            // giving access to Angular to manage them.
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry
                    .addResourceHandler("/**")
                    .addResourceLocations("classpath:/static/")
                    .resourceChain(true)
                    .addResolver(
                        new PathResourceResolver() {
                            @Override
                            protected Resource getResource(String resourcePath, Resource location) throws IOException {
                                Resource requestedResource = location.createRelative(resourcePath);
                                return requestedResource.exists() && requestedResource.isReadable()
                                    ? requestedResource
                                    : new ClassPathResource("/static/index.html");
                            }
                        }
                    );
            }
        };
    }
}
