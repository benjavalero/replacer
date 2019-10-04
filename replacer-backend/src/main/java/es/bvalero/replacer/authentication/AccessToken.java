package es.bvalero.replacer.authentication;

import lombok.Data;

@Data
public class AccessToken {
    private String token;
    private String tokenSecret;
}
