package es.bvalero.replacer.authentication.mediawiki;

import com.github.scribejava.apis.MediaWikiApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OAuthMediaWikiConfiguration {

    @Value("${replacer.wikipedia.api.key}")
    private String apiKey;

    @Value("${replacer.wikipedia.api.secret}")
    private String apiSecret;

    @Bean("oAuthMediaWikiService")
    public OAuth10aService oAuthMediaWikiService() {
        return new ServiceBuilder(apiKey).apiSecret(apiSecret).callback("oob").build(MediaWikiApi.instance());
    }
}
