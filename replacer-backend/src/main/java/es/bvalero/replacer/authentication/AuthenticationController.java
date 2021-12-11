package es.bvalero.replacer.authentication;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.exception.ReplacerException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** REST controller to perform authentication operations */
@Api(tags = "authentication")
@Loggable
@RestController
@RequestMapping("api/authentication")
public class AuthenticationController {

    @Autowired
    private OAuthService oAuthService;

    @Autowired
    private UserAuthenticator userAuthenticator;

    // Note these are the only REST endpoints which don't receive the common query parameters

    @ApiOperation(value = "Generate a request token, along with the authorization URL, to start authentication.")
    @GetMapping(value = "/request-token")
    public RequestTokenResponse getRequestToken() throws ReplacerException {
        RequestToken requestToken = oAuthService.getRequestToken();
        String authorizationUrl = oAuthService.getAuthorizationUrl(requestToken);
        return RequestTokenResponse.of(requestToken.getToken(), requestToken.getTokenSecret(), authorizationUrl);
    }

    @ApiOperation(value = "Verify the OAuth authentication and return the authenticated user details")
    @PostMapping(value = "/authenticate")
    public AuthenticatedUser authenticateUser(@Valid @RequestBody AuthenticateRequest authenticateRequest)
        throws ReplacerException {
        RequestToken requestToken = RequestToken.of(
            authenticateRequest.getToken(),
            authenticateRequest.getTokenSecret()
        );
        String oAuthVerifier = authenticateRequest.getOauthVerifier();
        AccessToken accessToken = oAuthService.getAccessToken(requestToken, oAuthVerifier);
        return userAuthenticator.getAuthenticatedUser(authenticateRequest.getLang(), accessToken);
    }
}
