package es.bvalero.replacer.authentication;

import lombok.Data;

@Data
class RequestToken {
    private String token;
    private String tokenSecret;
    private String url;
}
