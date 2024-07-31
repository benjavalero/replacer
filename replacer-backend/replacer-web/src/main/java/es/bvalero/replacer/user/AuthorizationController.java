package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.resolver.UserLanguage;
import es.bvalero.replacer.common.util.WebUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller to perform user authorization operations */
@Tag(name = "User")
@Slf4j
@PrimaryAdapter
@RestController
@RequestMapping("api/user")
class AuthorizationController {

    // Dependency injection
    private final AuthorizationApi authorizationApi;

    AuthorizationController(AuthorizationApi authorizationApi) {
        this.authorizationApi = authorizationApi;
    }

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
    // We include the endpoints under the User resource, with custom verbs for both actions.

    // Note these are the only REST endpoints which don't expect to receive the language header and the token cookie

    @Operation(summary = "Get a request token with an authorization URL to initiate an authorization process")
    @GetMapping(value = "/initiate-authorization")
    RequestTokenDto initiateAuthorization() {
        LOGGER.info("START Initiate Authorization");
        RequestTokenDto requestToken = RequestTokenDto.of(authorizationApi.getRequestToken());
        LOGGER.info("END Initiate Authorization: {}", requestToken);
        return requestToken;
    }

    @Operation(summary = "Verify the authorization process and get the authenticated user")
    @PostMapping(value = "/verify-authorization")
    ResponseEntity<UserDto> verifyAuthorization(
        @UserLanguage WikipediaLanguage lang,
        @Valid @RequestBody VerifyAuthorizationRequest verifyAuthorizationRequest
    ) {
        LOGGER.info("START Verify Authorization: {}", verifyAuthorizationRequest);
        RequestToken requestToken = RequestTokenDto.toDomain(verifyAuthorizationRequest.getRequestToken());
        String oAuthVerifier = verifyAuthorizationRequest.getOauthVerifier();
        User authenticatedUser = authorizationApi.getAuthenticatedUser(lang, requestToken, oAuthVerifier);
        UserDto authenticatedUserDto = UserDto.of(authenticatedUser);
        LOGGER.info("END Verify Authorization: {}", authenticatedUserDto);
        return ResponseEntity.ok()
            .header(
                HttpHeaders.SET_COOKIE,
                WebUtils.buildAccessTokenResponseCookie(authenticatedUser.getAccessToken()).toString()
            )
            .body(authenticatedUserDto);
    }
}
