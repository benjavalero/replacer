package es.bvalero.replacer.authentication;

import lombok.Value;

/** Sub-domain object representing an OAuth request token */
@Value(staticConstructor = "of")
class RequestToken {

    String token;
    String tokenSecret;

    // For testing and offline usage
    static RequestToken empty() {
        return RequestToken.of("", "");
    }
}
