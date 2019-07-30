package es.bvalero.replacer.authentication;

import com.github.scribejava.apis.MediaWikiApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth10aService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

// We make this implementation public to be used by the finder benchmarks
@Slf4j
@Service
@Profile("default")
public class AuthenticationServiceImpl implements AuthenticationService {

    @Value("${wikipedia.api.key}")
    private String apiKey;

    @Value("${wikipedia.api.secret}")
    private String apiSecret;

    @Value("${replacer.admin.user}")
    private String adminUser;

    private OAuth10aService oAuthService;

    private OAuth10aService getOAuthService() {
        if (oAuthService == null) {
            oAuthService = new ServiceBuilder(apiKey)
                    .apiSecret(apiSecret)
                    .callback("oob")
                    .build(MediaWikiApi.instance());
        }
        return oAuthService;
    }

    @Override
    public String executeOAuthRequest(String apiUrl, Map<String, String> params, boolean post,
                                      @Nullable OAuth1AccessToken accessToken) throws AuthenticationException {
        boolean signed = accessToken != null && StringUtils.isNotBlank(accessToken.getToken());
        LOGGER.debug("START Execute OAuth Request. URL: {} - Params: {} - Post: {} - Signed: {}",
                apiUrl, params, post, signed);
        OAuthRequest request = createOAuthRequestWithParams(apiUrl, params, post);
        if (signed) {
            signOAuthRequest(request, accessToken);
        }

        try {
            Response response = getOAuthService().execute(request);
            String body = response.getBody();
            if (body == null || !response.isSuccessful()) {
                throw new AuthenticationException(String.format("Call not successful: %d - %s",
                        response.getCode(), response.getMessage()));
            }
            LOGGER.debug("END Execute OAuth Request. Response: {}", body);
            return body;
        } catch (InterruptedException e) {
            LOGGER.error("ERROR executing OAuth Request", e);
            Thread.currentThread().interrupt();
            throw new AuthenticationException();
        } catch (ExecutionException | IOException e) {
            LOGGER.error("ERROR executing OAuth Request", e);
            throw new AuthenticationException();
        }
    }

    private OAuthRequest createOAuthRequestWithParams(String apiUrl, Map<String, String> params, boolean post) {
        OAuthRequest request = new OAuthRequest(post ? Verb.POST : Verb.GET, apiUrl);
        addParametersToRequest(request, params);
        return request;
    }

    private void addParametersToRequest(OAuthRequest request, Map<String, String> params) {
        params.forEach(request::addParameter);
    }

    private void signOAuthRequest(OAuthRequest request, OAuth1AccessToken accessToken) {
        getOAuthService().signRequest(accessToken, request);
    }

    @Override
    public String getAuthorizationUrl(OAuth1RequestToken requestToken) {
        LOGGER.info("START Get Authorization URL from MediaWiki API. Request Token: {}", requestToken.getToken());
        String url = getOAuthService().getAuthorizationUrl(requestToken);
        LOGGER.info("END Get Authorization URL from MediaWiki API: {}", url);
        return url;
    }

    @Override
    public OAuth1RequestToken getRequestToken() throws AuthenticationException {
        try {
            LOGGER.info("START Get Request Token from MediaWiki API");
            OAuth1RequestToken token = getOAuthService().getRequestToken();
            LOGGER.info("END Get Request Token from MediaWiki API. Token: {}", token.getToken());
            return token;
        } catch (InterruptedException e) {
            LOGGER.error("ERROR getting Request Token from MediaWiki API", e);
            Thread.currentThread().interrupt();
            throw new AuthenticationException();
        } catch (ExecutionException | IOException e) {
            LOGGER.error("ERROR getting Request Token from MediaWiki API", e);
            throw new AuthenticationException();
        }
    }

    @Override
    public OAuth1AccessToken getAccessToken(OAuth1RequestToken requestToken, String oauthVerifier)
            throws AuthenticationException {
        try {
            LOGGER.info("START Get Access Token from MediaWiki API. Request Token: {}", requestToken.getToken());
            OAuth1AccessToken token = getOAuthService().getAccessToken(requestToken, oauthVerifier);
            LOGGER.info("END Get Access Token from MediaWiki API: {} / {}", token.getToken(), token.getTokenSecret());
            return token;
        } catch (InterruptedException e) {
            LOGGER.error("ERROR getting Access Token from MediaWiki API", e);
            Thread.currentThread().interrupt();
            throw new AuthenticationException();
        } catch (ExecutionException | IOException e) {
            LOGGER.error("ERROR getting Access Token from MediaWiki API", e);
            throw new AuthenticationException();
        }
    }

    @Override
    public boolean isAdminUser(String username) {
        return this.adminUser.equals(username);
    }

}
