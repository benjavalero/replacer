package es.bvalero.replacer.authentication;

import lombok.Data;

@Data
class OauthTokenDto {
    private String token;
    private String tokenSecret;
}
