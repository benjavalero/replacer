package es.bvalero.replacer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * This configuration will be applied with non-production profiles, in order to perform calls
 * from frontend without getting CORS warnings from certain browsers.
 */
@Profile("!default")
@Configuration
public class WebConfig {

    @Value("${replacer.cors.allowed.origins}")
    private String corsAllowedOrigins;

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**").allowedOrigins(corsAllowedOrigins).allowedMethods("GET", "POST");
            }
        };
    }
}
