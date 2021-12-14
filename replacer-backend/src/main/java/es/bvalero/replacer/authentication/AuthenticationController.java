package es.bvalero.replacer.authentication;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.authentication.authenticateuser.AuthenticateUserService;
import es.bvalero.replacer.authentication.authenticateuser.AuthenticatedUser;
import es.bvalero.replacer.authentication.oauth.RequestToken;
import es.bvalero.replacer.authentication.requesttoken.GetRequestTokenResponse;
import es.bvalero.replacer.authentication.requesttoken.GetRequestTokenService;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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
    private GetRequestTokenService getRequestTokenService;

    @Autowired
    private AuthenticateUserService authenticateUserService;

    // Note these are the only REST endpoints which don't receive the common query parameters

    @Operation(summary = "Generate a request token, along with the authorization URL, to start authentication.")
    @GetMapping(value = "/request-token")
    public GetRequestTokenResponse getRequestToken() throws AuthenticationException {
        return getRequestTokenService.get();
    }

    @Operation(summary = "Verify the OAuth authentication and return the authenticated user details")
    @PostMapping(value = "/authenticate")
    public AuthenticatedUser authenticateUser(
        @Parameter(
            description = "Language of the Wikipedia in use",
            schema = @Schema(type = "string", allowableValues = { "es", "gl" }),
            required = true
        ) @RequestParam String lang,
        @Valid @RequestBody AuthenticateRequest authenticateRequest
    ) throws AuthenticationException {
        RequestToken requestToken = RequestToken.of(
            authenticateRequest.getToken(),
            authenticateRequest.getTokenSecret()
        );
        return authenticateUserService.authenticateUser(
            WikipediaLanguage.valueOfCode(lang),
            requestToken,
            authenticateRequest.getOauthVerifier()
        );
    }
}
