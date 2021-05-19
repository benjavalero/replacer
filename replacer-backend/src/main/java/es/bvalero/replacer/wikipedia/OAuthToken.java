package es.bvalero.replacer.wikipedia;

import lombok.Value;

@Value(staticConstructor = "of")
public class OAuthToken {

    String token;
    String tokenSecret;

    static OAuthToken ofEmpty() {
        return OAuthToken.of("", "");
    }
}
