package es.bvalero.replacer.wikipedia;

import lombok.Value;
import org.jetbrains.annotations.TestOnly;

/** Domain object representing an OAuth access token */
@Value(staticConstructor = "of")
public class AccessToken {

    String token;
    String tokenSecret;

    @TestOnly
    public static AccessToken empty() {
        return AccessToken.of("", "");
    }

    public boolean isEmpty() {
        return AccessToken.empty().equals(this);
    }
}
