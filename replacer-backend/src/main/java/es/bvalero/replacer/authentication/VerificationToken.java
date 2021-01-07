package es.bvalero.replacer.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class VerificationToken {

    private RequestToken requestToken;
    private String token;
}
