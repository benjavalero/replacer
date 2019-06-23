package es.bvalero.replacer.authentication;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaService;
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

    @Autowired
    private WikipediaService wikipediaService;

    @GetMapping(value = "/requestToken")
    public OAuth1RequestToken getRequestToken() throws AuthenticationException {
        LOGGER.info("GET Request Token from MediaWiki API");
        return authenticationService.getRequestToken();
    }

    @GetMapping(value = "/authorizationUrl")
    public List<String> getAuthorizationUrl(@RequestParam String token) {
        LOGGER.info("GET Authorization URL from MediaWiki API. Token: {}", token);
        // We don't need the token secret in this call
        OAuth1RequestToken requestToken = new OAuth1RequestToken(token, "");

        // We need to return a JSON object so we fake it with a list with only one string
        return Collections.singletonList(authenticationService.getAuthorizationUrl(requestToken));
    }

    @GetMapping(value = "/accessToken")
    public OAuth1AccessToken getAccessToken(@RequestParam String token, @RequestParam String tokenSecret,
                                            @RequestParam String verificationToken) throws AuthenticationException {
        LOGGER.info("GET Access Token from MediaWiki API. Token: {} / {} / {}", token, tokenSecret, verificationToken);
        OAuth1RequestToken requestToken = new OAuth1RequestToken(token, tokenSecret);
        return authenticationService.getAccessToken(requestToken, verificationToken);
    }

    @GetMapping(value = "/username")
    public List<String> getUsername(@RequestParam String token, @RequestParam String tokenSecret)
            throws WikipediaException {
        LOGGER.info("GET Name of the logged user from Wikipedia API. Token: {} / {}", token, tokenSecret);
        OAuth1AccessToken accessToken = new OAuth1AccessToken(token, tokenSecret);

        // We need to return a JSON object so we fake it with a list with only one string
        return Collections.singletonList(wikipediaService.identify(accessToken));
    }

}
