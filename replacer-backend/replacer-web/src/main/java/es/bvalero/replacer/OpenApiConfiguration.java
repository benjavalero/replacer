package es.bvalero.replacer;

import es.bvalero.replacer.user.WebUtils;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@OpenAPIDefinition(servers = { @Server(url = "/") })
@Configuration
public class OpenApiConfiguration {

    @Bean
    public OperationCustomizer customize() {
        return (operation, handlerMethod) ->
            operation
                .addParametersItem(new Parameter().in("header").name(HttpHeaders.ACCEPT_LANGUAGE))
                .addParametersItem(new Parameter().in("cookie").name(WebUtils.ACCESS_TOKEN_COOKIE));
    }
}
