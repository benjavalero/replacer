package es.bvalero.replacer.authentication;

import lombok.Data;

@Data
class VerificationTokenDto {
    private RequestTokenDto requestToken;
    private String verificationToken;
}
