package es.bvalero.replacer.wikipedia;

import lombok.Value;

@Value(staticConstructor = "of")
class AccessToken {

    String token;
    String tokenSecret;

    static AccessToken ofEmpty() {
        return AccessToken.of("", "");
    }
}
