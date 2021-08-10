package es.bvalero.replacer.wikipedia;

import lombok.Value;

/** Domain object representing a standard OAuth pair token/secret */
@Value(staticConstructor = "of")
public class OAuthToken {

    String token;
    String tokenSecret;

    public static OAuthToken ofEmpty() {
        return OAuthToken.of("", "");
    }
}
