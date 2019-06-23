package es.bvalero.replacer.authentication;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface AuthenticationService {

    /**
     * Execute an OAuth Request on the Wikipedia API.
     *
     * @param params      A key-value map with the parameters sent on the request.
     * @param post        True if the request is to be done as POST. If not, the request will be GET.
     * @param accessToken If not null, the request will be signed with it.
     * @return The body response as a string.
     */
    String executeOAuthRequest(String apiUrl, Map<String, String> params, boolean post,
                               @Nullable OAuth1AccessToken accessToken) throws AuthenticationException;

    String getAuthorizationUrl(OAuth1RequestToken requestToken);

    OAuth1RequestToken getRequestToken() throws AuthenticationException;

    OAuth1AccessToken getAccessToken(OAuth1RequestToken requestToken, String oauthVerifier)
            throws AuthenticationException;

}