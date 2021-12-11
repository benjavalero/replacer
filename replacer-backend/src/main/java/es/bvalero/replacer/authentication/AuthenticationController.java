package es.bvalero.replacer.authentication;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** REST controller to perform authentication operations */
@Tag(name = "Authentication")
@Loggable
@RestController
@RequestMapping("api/authentication")
public class AuthenticationController {

    @Autowired
    private OAuthService oAuthService;

    @Autowired
    private UserAuthenticator userAuthenticator;

    // Note these are the only REST endpoints which don't receive the common query parameters

    @Operation(summary = "Generate a request token, along with the authorization URL, to start authentication.")
    @GetMapping(value = "/request-token")
    public RequestTokenResponse getRequestToken() throws ReplacerException {
        RequestToken requestToken = oAuthService.getRequestToken();
        String authorizationUrl = oAuthService.getAuthorizationUrl(requestToken);
        return RequestTokenResponse.of(requestToken.getToken(), requestToken.getTokenSecret(), authorizationUrl);
    }

    @Operation(summary = "Verify the OAuth authentication and return the authenticated user details")
    @PostMapping(value = "/authenticate")
    public AuthenticatedUser authenticateUser(
        @Parameter(
            description = "Language of the Wikipedia in use",
            required = true,
            example = "es"
        ) @RequestParam WikipediaLanguage lang,
        @Valid @RequestBody AuthenticateRequest authenticateRequest
    ) throws ReplacerException {
        RequestToken requestToken = RequestToken.of(
            authenticateRequest.getToken(),
            authenticateRequest.getTokenSecret()
        );
        String oAuthVerifier = authenticateRequest.getOauthVerifier();
        AccessToken accessToken = oAuthService.getAccessToken(requestToken, oAuthVerifier);
        return userAuthenticator.getAuthenticatedUser(lang, accessToken);
    }
}
