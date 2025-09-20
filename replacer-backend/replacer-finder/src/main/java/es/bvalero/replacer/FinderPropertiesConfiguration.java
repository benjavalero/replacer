package es.bvalero.replacer;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application-finder.properties")
@Data
public class FinderPropertiesConfiguration {

    @Value("replacer.show.immutable.warning")
    private String showImmutableWarning;
}
