package es.bvalero.replacer;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@SuppressWarnings("java:S1118")
@Configuration
public class DumpPropertiesConfiguration {

    @Configuration
    @PropertySource("classpath:application-dump.properties")
    @Profile("!default")
    public static class DumpDefaultConfiguration {}

    @Configuration
    @PropertySource({ "classpath:application-dump.properties", "classpath:application-dump-prod.properties" })
    @Profile("default")
    public static class DumpProductionConfiguration {}
}
