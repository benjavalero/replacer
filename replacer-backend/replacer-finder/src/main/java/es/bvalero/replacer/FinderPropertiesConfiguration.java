package es.bvalero.replacer;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application-finder.properties")
public class FinderPropertiesConfiguration {}
