package es.bvalero.replacer.authentication;

import lombok.Data;

@Data
public class AccessTokenDto {
    private String token;
    private String tokenSecret;
}
