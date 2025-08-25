package es.bvalero.replacer;

import com.github.scribejava.apis.MediaWikiApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MediaWikiConfiguration {

    @Value("${replacer.wikipedia.api.key}")
    private String apiKey;

    @Value("${replacer.wikipedia.api.secret}")
    private String apiSecret;

    @Bean("mediaWikiService")
    public OAuth10aService mediaWikiService() {
        return new ServiceBuilder(this.apiKey).apiSecret(this.apiSecret).callback("oob")
            .userAgent("BenjaBot/0.0 (https://replacer.toolforge.org) scribejava/8.3.3")
            .build(MediaWikiApi.instance());
    }
}
