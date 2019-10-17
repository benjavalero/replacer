package es.bvalero.replacer.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class RequestToken {
    private String token;
    private String tokenSecret;
    private String url;
}
