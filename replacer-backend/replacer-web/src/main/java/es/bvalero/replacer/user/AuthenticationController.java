package es.bvalero.replacer.user;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
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
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;

    // The usual OAuth1 workflow consists in 3 steps:
    // See: https://oauth1.wp-api.org/docs/basics/Auth-Flow.html
    // 1. Obtain temporary credentials for the initial authorization process (Request Token)
    // 2. Redirect to the Authorization URL where the client actually logs in
    // 3. Exchange the temporary credentials for long-lived ones (Access Token)
    //    including a verifier token from the previous authorization step
    // We include an additional step:
    // 4. Retrieve the details of the user from Wikipedia

    // For the sake of simplicity, and to reduce the amount of calls, we only serve 2 endpoints/steps:
    // A. Initiate (steps 1 and 2)
    // B. Verify (steps 3 and 4)
    // We also orchestrate the steps in the controller,
    // but we create a simple service to keep right directions in module dependencies.

    // Note these are the only REST endpoints which don't receive the common query parameters

    @Operation(summary = "Initiate an authorization process")
    @GetMapping(value = "/initiate")
    public InitiateAuthenticationResponse initiateAuthentication() throws AuthenticationException {
        RequestToken requestToken = authenticationService.getRequestToken();
        String authorizationUrl = authenticationService.getAuthorizationUrl(requestToken);
        return InitiateAuthenticationResponse.of(RequestTokenDto.of(requestToken), authorizationUrl);
    }

    @Operation(summary = "Verify the authorization process")
    @PostMapping(value = "/verify")
    public VerifyAuthenticationResponse verifyAuthentication(
        @Parameter(description = "Language of the Wikipedia in use", required = true, example = "es") @RequestParam String lang,
        @Valid @RequestBody VerifyAuthenticationRequest verifyAuthenticationRequest
    ) throws AuthenticationException {
        RequestToken requestToken = RequestTokenDto.toDomain(verifyAuthenticationRequest.getRequestToken());
        String oAuthVerifier = verifyAuthenticationRequest.getOauthVerifier();
        AccessToken accessToken = authenticationService.getAccessToken(requestToken, oAuthVerifier);
        User user = userService
            .findAuthenticatedUser(WikipediaLanguage.valueOfCode(lang), accessToken)
            .orElseThrow(AuthenticationException::new);
        return VerifyAuthenticationResponse.of(user, accessToken);
    }
}
