package es.bvalero.replacer.wikipedia;

import lombok.Value;
import org.jetbrains.annotations.TestOnly;

/** Domain object representing an OAuth access token */
@Value(staticConstructor = "of")
public class OAuthToken {

    String token;
    String tokenSecret;

    @TestOnly
    public static OAuthToken empty() {
        return OAuthToken.of("", "");
    }

    public boolean isEmpty() {
        return OAuthToken.empty().equals(this);
    }
}
