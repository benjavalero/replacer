package es.bvalero.replacer.config;

import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

// http://localhost:8080/swagger-ui/index.html
// http://localhost:8080/v2/api-docs
// http://localhost:8080/v3/api-docs

@Profile("!default")
@Configuration
public class SpringFoxConfiguration {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2) // OAS_30
            .select()
            .apis(RequestHandlerSelectors.basePackage("es.bvalero.replacer"))
            .paths(PathSelectors.regex("/api.*"))
            .build()
            .consumes(Set.of(MediaType.APPLICATION_JSON_VALUE))
            .produces(Set.of(MediaType.APPLICATION_JSON_VALUE))
            .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("Replacer REST API").version("2.9.3").build();
    }
}
