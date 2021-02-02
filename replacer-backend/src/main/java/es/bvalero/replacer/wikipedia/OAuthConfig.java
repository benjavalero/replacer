package es.bvalero.replacer.wikipedia;

import com.github.scribejava.apis.MediaWikiApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OAuthConfig {

    @Value("${replacer.wikipedia.api.key}")
    private String apiKey;

    @Value("${replacer.wikipedia.api.secret}")
    private String apiSecret;

    @Bean
    public OAuth10aService oAuthService() {
        return new ServiceBuilder(apiKey).apiSecret(apiSecret).callback("oob").build(MediaWikiApi.instance());
    }
}
