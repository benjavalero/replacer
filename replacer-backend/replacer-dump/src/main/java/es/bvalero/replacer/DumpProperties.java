package es.bvalero.replacer;

import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "replacer.dump")
@PropertySource(value = "classpath:application-dump.yml", factory = YamlPropertySourceFactory.class)
@Data
public class DumpProperties {

    private Map<String, Integer> numPagesEstimated;
}
