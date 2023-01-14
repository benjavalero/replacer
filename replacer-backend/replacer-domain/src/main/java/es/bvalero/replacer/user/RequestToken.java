package es.bvalero.replacer.user;

import lombok.Value;
import org.springframework.lang.NonNull;

/** Wikipedia OAuth request token */
@Value(staticConstructor = "of")
class RequestToken {

    @NonNull
    String token;

    @NonNull
    String tokenSecret;
}
