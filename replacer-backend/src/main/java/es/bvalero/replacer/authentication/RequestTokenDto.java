package es.bvalero.replacer.authentication;

import lombok.Data;

@Data
class RequestTokenDto {
    private String token;
    private String tokenSecret;
    private String url;
}
