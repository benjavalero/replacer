package es.bvalero.replacer;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@SuppressWarnings("java:S1118")
@Configuration
public class WikipediaPropertiesConfiguration {

    // Note that the profile for Production is "default"

    @Configuration
    @PropertySource("classpath:application-wikipedia.properties")
    @Profile("!default")
    public static class NonProductionDefaultConfiguration {}

    @Configuration
    @PropertySource("classpath:application-wikipedia-prod.properties")
    @Profile("default")
    public static class WikipediaProductionConfiguration {}
}
