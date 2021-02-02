package es.bvalero.replacer.authentication;

import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class AccessToken {

    String token;
    String tokenSecret;
}
