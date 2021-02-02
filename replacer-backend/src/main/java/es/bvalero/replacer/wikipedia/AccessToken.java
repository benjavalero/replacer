package es.bvalero.replacer.wikipedia;

import lombok.Value;
import org.jetbrains.annotations.TestOnly;

@Value(staticConstructor = "of")
public class AccessToken {

    String token;
    String tokenSecret;

    @TestOnly
    static AccessToken ofEmpty() {
        return AccessToken.of("", "");
    }
}
