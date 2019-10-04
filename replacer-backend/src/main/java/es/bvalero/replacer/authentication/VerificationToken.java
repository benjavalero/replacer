package es.bvalero.replacer.authentication;

import lombok.Data;

@Data
class VerificationToken {
    private RequestToken requestToken;
    private String verificationToken;
}
