package es.bvalero.replacer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonMapperConfiguration {

    @Bean
    public ObjectMapper jsonMapper() {
        return buildJsonMapper();
    }

    public static ObjectMapper buildJsonMapper() {
        // We override the default object mapper
        // just in case we return an object containing an optional field.
        // This is supposed not to be needed since Jackson 3.x
        // https://github.com/FasterXML/jackson-modules-java8
        return JsonMapper.builder().addModule(new Jdk8Module()).addModule(new JavaTimeModule()).build();
    }
}
