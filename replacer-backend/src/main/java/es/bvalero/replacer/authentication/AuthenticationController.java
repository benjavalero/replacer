package es.bvalero.replacer.authentication;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaUser;
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
    private AuthenticationService authenticationService;

    @Autowired
    private WikipediaService wikipediaService;

    // Note these are the only REST endpoints which don't receive the common query parameters

    @ApiOperation(value = "Generate a request token, along with the authorization URL, to start authentication")
    @GetMapping(value = "/request-token")
    public RequestTokenResponse getRequestToken() throws ReplacerException {
        RequestToken requestToken = authenticationService.getRequestToken();
        String authorizationUrl = authenticationService.getAuthorizationUrl(requestToken);
        return RequestTokenResponse.of(requestToken, authorizationUrl);
    }

    @ApiOperation(value = "Verify the OAuth authentication and return the authenticated user details")
    @PostMapping(value = "/authenticate")
    public AuthenticateResponse authenticate(@Valid @RequestBody AuthenticateRequest authenticateRequest)
        throws ReplacerException {
        RequestToken requestToken = authenticateRequest.getRequestToken();
        String oAuthVerifier = authenticateRequest.getOauthVerifier();
        AccessToken accessToken = authenticationService.getAccessToken(requestToken, oAuthVerifier);
        WikipediaUser wikipediaUser = wikipediaService.getAuthenticatedUser(authenticateRequest.getLang(), accessToken);
        return AuthenticateResponse.of(accessToken, wikipediaUser);
    }
}
