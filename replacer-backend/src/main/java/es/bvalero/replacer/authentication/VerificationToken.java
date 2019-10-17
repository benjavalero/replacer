package es.bvalero.replacer.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class VerificationToken {
    private RequestToken requestToken;
    private String token;
}
