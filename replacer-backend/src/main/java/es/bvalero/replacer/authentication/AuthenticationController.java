package es.bvalero.replacer.authentication;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.authentication.dto.InitiateAuthenticationResponse;
import es.bvalero.replacer.authentication.dto.RequestTokenDto;
import es.bvalero.replacer.authentication.dto.VerifyAuthenticationRequest;
import es.bvalero.replacer.authentication.dto.VerifyAuthenticationResponse;
import es.bvalero.replacer.authentication.oauth.OAuthService;
import es.bvalero.replacer.authentication.oauth.RequestToken;
import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.ReplacerUser;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.dto.AccessTokenDto;
import es.bvalero.replacer.user.UserService;
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
    private OAuthService oAuthService;

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
    // We also orchestrate the steps in the controller, it is too simple to create dedicated services.

    // Note these are the only REST endpoints which don't receive the common query parameters

    @Operation(summary = "Initiate an authorization process")
    @GetMapping(value = "/initiate")
    public InitiateAuthenticationResponse initiateAuthentication() throws AuthenticationException {
        RequestToken requestToken = oAuthService.getRequestToken();
        String authorizationUrl = oAuthService.getAuthorizationUrl(requestToken);
        return InitiateAuthenticationResponse.of(RequestTokenDto.fromDomain(requestToken), authorizationUrl);
    }

    @Operation(summary = "Verify the authorization process")
    @PostMapping(value = "/verify")
    public VerifyAuthenticationResponse verifyAuthentication(
        @Parameter(
            description = "Language of the Wikipedia in use",
            schema = @Schema(type = "string", allowableValues = { "es", "gl" }),
            required = true
        ) @RequestParam String lang,
        @Valid @RequestBody VerifyAuthenticationRequest verifyAuthenticationRequest
    ) throws AuthenticationException {
        RequestToken requestToken = RequestTokenDto.toDomain(verifyAuthenticationRequest.getRequestToken());
        String oAuthVerifier = verifyAuthenticationRequest.getOauthVerifier();
        AccessToken accessToken = oAuthService.getAccessToken(requestToken, oAuthVerifier);
        ReplacerUser user = userService
            .findUser(WikipediaLanguage.valueOfCode(lang), accessToken)
            .orElseThrow(AuthenticationException::new);

        return VerifyAuthenticationResponse
            .builder()
            .name(user.getName())
            .hasRights(user.hasRights())
            .bot(user.isBot())
            .admin(user.isAdmin())
            .accessToken(AccessTokenDto.fromDomain(accessToken))
            .build();
    }
}
