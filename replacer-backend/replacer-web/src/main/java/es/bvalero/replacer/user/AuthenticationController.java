package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller to perform authentication operations */
@Tag(name = "Authentication")
@Slf4j
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
        LOGGER.info("START Initiate Authentication");
        RequestToken requestToken = authenticationService.getRequestToken();
        String authorizationUrl = authenticationService.getAuthorizationUrl(requestToken);
        InitiateAuthenticationResponse response = InitiateAuthenticationResponse.of(
            RequestTokenDto.of(requestToken),
            authorizationUrl
        );
        LOGGER.info("END Initiate Authentication: {}", response);
        return response;
    }

    @Operation(summary = "Verify the authorization process")
    @PostMapping(value = "/verify")
    public ResponseEntity<UserDto> verifyAuthentication(
        @UserLanguage WikipediaLanguage lang,
        @Valid @RequestBody VerifyAuthenticationRequest verifyAuthenticationRequest
    ) throws AuthenticationException {
        LOGGER.info("START Verify Authentication: {}", verifyAuthenticationRequest);
        RequestToken requestToken = RequestTokenDto.toDomain(verifyAuthenticationRequest.getRequestToken());
        String oAuthVerifier = verifyAuthenticationRequest.getOauthVerifier();
        AccessToken accessToken = authenticationService.getAccessToken(requestToken, oAuthVerifier);
        User user = userService.findAuthenticatedUser(lang, accessToken).orElseThrow(AuthenticationException::new);
        UserDto userDto = UserDto.of(user);
        LOGGER.info("END Verify Authentication: {}", userDto);
        return ResponseEntity
            .ok()
            .header(HttpHeaders.SET_COOKIE, buildAccessTokenCookie(accessToken).toString())
            .body(userDto);
    }

    private ResponseCookie buildAccessTokenCookie(AccessToken accessToken) {
        // Max age 400 days: https://developer.chrome.com/blog/cookie-max-age-expires/
        // Domain: default
        // SameSite is Lax by default, but it fails in some old browsers, so we set it explicitly.
        return ResponseCookie
            .from(AccessToken.COOKIE_NAME, accessToken.toCookieValue())
            .maxAge(400 * 24 * 3600)
            .path("/api")
            .secure(true)
            .httpOnly(true)
            .sameSite("Lax")
            .build();
    }
}
