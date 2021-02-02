package es.bvalero.replacer.wikipedia;

import lombok.Value;

@Value(staticConstructor = "of")
class RequestToken {

    String token;
    String tokenSecret;
    String authorizationUrl;
}
