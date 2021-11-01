package es.bvalero.replacer.authentication;

import lombok.Value;

/** Sub-domain object representing an OAuth request token */
@Value(staticConstructor = "of")
public class RequestToken {

    String token;
    String tokenSecret;
}
