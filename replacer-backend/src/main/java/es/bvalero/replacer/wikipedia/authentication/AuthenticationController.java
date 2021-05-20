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

    @ApiOperation(value = "Generate a request token to start OAuth authentication in MediaWiki")
    @GetMapping(value = "/request-token")
    public RequestToken getRequestToken() throws ReplacerException {
        return authenticationService.getRequestToken();
    }

    @ApiOperation(
        value = "Retrieve the user (already authenticated in MediaWiki) and the access token for further operations"
    )
    @PostMapping(value = "/logged-user")
    public WikipediaUser getLoggedUser(
        @ApiParam(value = "Language", allowableValues = "es, gl", required = true) @RequestParam WikipediaLanguage lang,
        @ApiParam(
            value = "Verification token received after MediaWiki authentication"
        ) @RequestBody VerificationToken verificationToken
    ) throws ReplacerException {
        return authenticationService.getLoggedUser(
            lang,
            OAuthToken.of(verificationToken.getRequestToken(), verificationToken.getRequestTokenSecret()),
            verificationToken.getOauthVerifier()
        );
    }
}
