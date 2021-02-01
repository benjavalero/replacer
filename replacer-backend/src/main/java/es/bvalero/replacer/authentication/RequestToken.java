package es.bvalero.replacer.authentication;

import lombok.Value;

@Value(staticConstructor = "of")
class RequestToken {

    String token;
    String tokenSecret;
    String authorizationUrl;
}
