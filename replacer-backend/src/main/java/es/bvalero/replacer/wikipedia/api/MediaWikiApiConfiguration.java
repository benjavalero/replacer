package es.bvalero.replacer.wikipedia.api;

import com.github.scribejava.apis.MediaWikiApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class MediaWikiApiConfiguration {

    @Value("${replacer.wikipedia.api.key}")
    private String apiKey;

    @Value("${replacer.wikipedia.api.secret}")
    private String apiSecret;

    @Bean("mediaWikiApiService")
    public OAuth10aService mediaWikiApiService() {
        return new ServiceBuilder(apiKey).apiSecret(apiSecret).callback("oob").build(MediaWikiApi.instance());
    }
}
