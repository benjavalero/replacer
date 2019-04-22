package es.bvalero.replacer.authentication;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("authentication")
public class AuthenticationController {

    @Autowired
    private IAuthenticationService authenticationService;

    @GetMapping(value = "/requestToken")
    public OAuth1RequestToken getRequestToken() throws InterruptedException, ExecutionException, IOException {
        return authenticationService.getRequestToken();
    }

    @GetMapping(value = "/authorizationUrl")
    public List<String> getAuthorizationUrl(@RequestParam String token) {
        // We don't need the token secret in this call
        OAuth1RequestToken requestToken = new OAuth1RequestToken(token, "");

        // We need to return a JSON object so we fake it with a list of strings
        return Collections.singletonList(authenticationService.getAuthorizationUrl(requestToken));
    }

    @GetMapping(value = "/accessToken")
    public OAuth1AccessToken getAccessToken(@RequestParam String token, @RequestParam String tokenSecret, @RequestParam String verificationToken)
            throws InterruptedException, ExecutionException, IOException {
        OAuth1RequestToken requestToken = new OAuth1RequestToken(token, tokenSecret);
        return authenticationService.getAccessToken(requestToken, verificationToken);
    }

}
