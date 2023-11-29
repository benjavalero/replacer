package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.resolver.UserLanguage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
    private final AuthorizationService authorizationService;
    private final UserService userService;

    AuthorizationController(AuthorizationService authorizationService, UserService userService) {
        this.authorizationService = authorizationService;
        this.userService = userService;
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
    // We also orchestrate the steps in the controller,
    // but we create a simple service to keep right directions in module dependencies.
    // We include the endpoints under the User resource, with custom verbs for both actions.

    // Note these are the only REST endpoints which don't expect to receive the language header and the token cookie

    @Operation(summary = "Initiate an authorization process")
    @GetMapping(value = "/initiate-authorization")
    InitiateAuthorizationResponse initiateAuthorization() {
        LOGGER.info("START Initiate Authorization");
        RequestToken requestToken = authorizationService.getRequestToken();
        String authorizationUrl = authorizationService.getAuthorizationUrl(requestToken);
        InitiateAuthorizationResponse initiateAuthorizationResponse = InitiateAuthorizationResponse.of(
            RequestTokenDto.of(requestToken),
            authorizationUrl
        );
        LOGGER.info("END Initiate Authorization: {}", initiateAuthorizationResponse);
        return initiateAuthorizationResponse;
    }

    @Operation(summary = "Verify the authorization process")
    @PostMapping(value = "/verify-authorization")
    ResponseEntity<UserDto> verifyAuthorization(
        @UserLanguage WikipediaLanguage lang,
        @Valid @RequestBody VerifyAuthorizationRequest verifyAuthorizationRequest
    ) {
        LOGGER.info("START Verify Authorization: {}", verifyAuthorizationRequest);
        RequestToken requestToken = RequestTokenDto.toDomain(verifyAuthorizationRequest.getRequestToken());
        String oAuthVerifier = verifyAuthorizationRequest.getOauthVerifier();
        AccessToken accessToken = authorizationService.getAccessToken(requestToken, oAuthVerifier);
        User authenticatedUser = userService
            .findAuthenticatedUser(lang, accessToken)
            .orElseThrow(AuthorizationException::new);
        UserDto authenticatedUserDto = UserDto.of(authenticatedUser);
        LOGGER.info("END Verify Authorization: {}", authenticatedUserDto);
        return ResponseEntity
            .ok()
            .header(HttpHeaders.SET_COOKIE, buildAccessTokenCookie(accessToken).toString())
            .body(authenticatedUserDto);
    }

    private ResponseCookie buildAccessTokenCookie(AccessToken accessToken) {
        // Max age 400 days: https://developer.chrome.com/blog/cookie-max-age-expires/
        // Domain: default
        // SameSite is Lax by default, but it fails in some old browsers, so we set it explicitly.
        return ResponseCookie
            .from(AccessToken.COOKIE_NAME, accessToken.toCookieValue())
            .maxAge((long) 400 * 24 * 3600)
            .path("/api")
            .secure(true)
            .httpOnly(true)
            .sameSite("Lax")
            .build();
    }
}
