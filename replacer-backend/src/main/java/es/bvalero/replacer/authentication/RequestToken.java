package es.bvalero.replacer.authentication;

import lombok.Value;
import org.jetbrains.annotations.TestOnly;

/** Sub-domain object representing an OAuth request token */
@Value(staticConstructor = "of")
class RequestToken {

    String token;
    String tokenSecret;

    @TestOnly
    static RequestToken empty() {
        return RequestToken.of("", "");
    }
}
