package es.bvalero.replacer;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
public class CorePropertiesConfiguration {

    @Configuration
    @PropertySource("classpath:application-core.properties")
    @Profile("!offline")
    public static class CoreDefaultConfiguration {}

    @Configuration
    @PropertySource({ "classpath:application-core.properties", "classpath:application-core-offline.properties" })
    @Profile("offline")
    public static class CoreOfflineConfiguration {}

    @Configuration
    @PropertySource({ "classpath:application-core.properties", "classpath:application-core-prod.properties" })
    @Profile("default")
    public static class CoreProductionConfiguration {}
}
