package es.bvalero.replacer.common.domain;

import lombok.Value;
import org.jetbrains.annotations.TestOnly;

// TODO: Check if it is actually a domain object
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
