package es.bvalero.replacer.auth;

import lombok.Value;
import org.springframework.lang.NonNull;

/** Request token with an authorization URL to initiate an authorization process */
@Value(staticConstructor = "of")
public class RequestToken {

    @NonNull
    String token;

    @NonNull
    String tokenSecret;

    @NonNull
    String authorizationUrl;
}
