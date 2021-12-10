package es.bvalero.replacer.common.domain;

import lombok.Value;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;

/** Wikipedia OAuth access token to log in the application and be able to apply changes in Wikipedia */
@Value(staticConstructor = "of")
public class AccessToken {

    @NonNull
    String token;

    @NonNull
    String tokenSecret;

    @TestOnly
    public static AccessToken empty() {
        return AccessToken.of("", "");
    }

    public boolean isEmpty() {
        return AccessToken.empty().equals(this);
    }
}
