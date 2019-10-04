package es.bvalero.replacer.authentication;

import lombok.Data;

@Data
class AccessTokenDto {
    private String token;
    private String tokenSecret;
}
