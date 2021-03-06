package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.ReplacerException;
import java.util.Map;

/**
 * Service to perform OAuth1 operations like authentication and signed HTTP requests
 */
public interface OAuthService {
    OAuthToken getRequestToken() throws ReplacerException;

    String getAuthorizationUrl(OAuthToken requestToken) throws ReplacerException;

    OAuthToken getAccessToken(OAuthToken requestToken, String oAuthVerifier) throws ReplacerException;

    String executeRequest(String verb, String url, Map<String, String> parameters) throws ReplacerException;

    String executeSignedRequest(String verb, String url, Map<String, String> parameters, OAuthToken oAuthToken)
        throws ReplacerException;
}
