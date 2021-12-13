package es.bvalero.replacer.authentication.oauth;

import lombok.Value;
import org.springframework.lang.NonNull;

/** Wikipedia OAuth request token */
@Value(staticConstructor = "of")
public class RequestToken {

    @NonNull
    String token;

    @NonNull
    String tokenSecret;
}
