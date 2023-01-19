package es.bvalero.replacer;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(servers = { @Server(url = "/") })
@Configuration
public class OpenApiConfiguration {}
