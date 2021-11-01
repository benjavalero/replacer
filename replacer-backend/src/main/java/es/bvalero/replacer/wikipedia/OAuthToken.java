package es.bvalero.replacer.wikipedia;

import lombok.Value;

/** Domain object representing a standard OAuth pair token/secret */
@Value(staticConstructor = "of")
public class OAuthToken {

    String token;
    String tokenSecret;

    // For testing and offline usage
    public static OAuthToken empty() {
        return OAuthToken.of("", "");
    }

    public boolean isEmpty() {
        return OAuthToken.empty().equals(this);
    }
}
