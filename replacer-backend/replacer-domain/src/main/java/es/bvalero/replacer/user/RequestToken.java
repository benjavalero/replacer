package es.bvalero.replacer.user;

import lombok.Value;
import org.springframework.lang.NonNull;

/** Request token with an authorization URL to initiate an authorization process */
@Value(staticConstructor = "of")
class RequestToken {

    @NonNull
    String token;

    @NonNull
    String tokenSecret;

    @NonNull
    String authorizationUrl;
}
