package es.bvalero.replacer.authentication;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.OAuthToken;

/** Service to perform authentication operations */
public interface AuthenticationService {
    RequestToken getRequestToken() throws ReplacerException;

    AuthenticateResponse authenticate(WikipediaLanguage lang, OAuthToken requestToken, String oAuthVerifier)
        throws ReplacerException;
}
