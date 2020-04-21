package es.bvalero.replacer;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({ "classpath*:applicationContext.xml" })
public class XmlConfiguration {}
