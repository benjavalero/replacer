package es.bvalero.replacer.wikipedia.authentication;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.OAuthToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "authentication")
@Loggable(prepend = true)
@RestController
@RequestMapping("api/authentication")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @ApiOperation(value = "Generate a request token to start OAuth authentication")
    @GetMapping(value = "/request-token")
    public RequestToken getRequestToken() throws ReplacerException {
        return authenticationService.getRequestToken();
    }

    @ApiOperation(value = "Verify the OAuth authentication and return the authenticated user details")
    @PostMapping(value = "/authenticate")
    public AuthenticateResponse authenticate(
        @ApiParam(value = "Language", allowableValues = "es, gl", required = true) @RequestParam String lang,
        @RequestBody AuthenticateRequest authenticateRequest
    ) throws ReplacerException {
        return authenticationService.authenticate(
            WikipediaLanguage.forValues(lang),
            OAuthToken.of(authenticateRequest.getRequestToken(), authenticateRequest.getRequestTokenSecret()),
            authenticateRequest.getOauthVerifier()
        );
    }
}
