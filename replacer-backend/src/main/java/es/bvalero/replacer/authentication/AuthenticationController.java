package es.bvalero.replacer.authentication;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("authentication")
public class AuthenticationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping(value = "/requestToken")
    public OAuth1RequestToken getRequestToken() throws AuthenticationException {
        LOGGER.info("GET request token");
        return authenticationService.getRequestToken();
    }

    @GetMapping(value = "/authorizationUrl")
    public List<String> getAuthorizationUrl(@RequestParam String token) throws AuthenticationException {
        LOGGER.info("GET authorization URL");
        // We don't need the token secret in this call
        OAuth1RequestToken requestToken = new OAuth1RequestToken(token, "");

        // We need to return a JSON object so we fake it with a list with only one string
        return Collections.singletonList(authenticationService.getAuthorizationUrl(requestToken));
    }

    @GetMapping(value = "/accessToken")
    public OAuth1AccessToken getAccessToken(@RequestParam String token, @RequestParam String tokenSecret,
                                            @RequestParam String verificationToken)
            throws AuthenticationException {
        LOGGER.info("GET access token");
        OAuth1RequestToken requestToken = new OAuth1RequestToken(token, tokenSecret);
        return authenticationService.getAccessToken(requestToken, verificationToken);
    }

}
