package es.bvalero.replacer.wikipedia.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class JsonMapperConfiguration {

    @Bean
    public ObjectMapper jsonMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // We override the default object mapper
        // just in case we return an object containing an optional field.
        // This is supposed not to be needed since Jackson 3.x
        // https://github.com/FasterXML/jackson-modules-java8
        mapper.registerModule(new Jdk8Module());
        return mapper;
    }
}
