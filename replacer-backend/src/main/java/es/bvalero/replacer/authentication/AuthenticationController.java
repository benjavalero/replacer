package es.bvalero.replacer.authentication;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.OAuthToken;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** REST controller to perform authentication operations */
@Api(tags = "authentication")
@Loggable(prepend = true)
@RestController
@RequestMapping("api/authentication")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private WikipediaService wikipediaService;

    @ApiOperation(value = "Generate a request token, along with the authorization URL, to start authentication")
    @GetMapping(value = "/request-token")
    public RequestTokenResponse getRequestToken() throws ReplacerException {
        RequestToken requestToken = authenticationService.getRequestToken();
        String authorizationUrl = authenticationService.getAuthorizationUrl(requestToken);
        return RequestTokenResponse.of(requestToken, authorizationUrl);
    }

    @ApiOperation(value = "Verify the OAuth authentication and return the authenticated user details")
    @PostMapping(value = "/authenticate")
    public AuthenticateResponse authenticate(
        @ApiParam(value = "Language", allowableValues = "es, gl", required = true) @RequestParam WikipediaLanguage lang,
        @Valid @RequestBody AuthenticateRequest authenticateRequest
    ) throws ReplacerException {
        RequestToken requestToken = authenticateRequest.getRequestToken();
        String oAuthVerifier = authenticateRequest.getOauthVerifier();
        OAuthToken accessToken = authenticationService.getAccessToken(requestToken, oAuthVerifier);
        WikipediaUser wikipediaUser = wikipediaService.getAuthenticatedUser(lang, accessToken);
        return AuthenticateResponse.of(accessToken, wikipediaUser);
    }
}
