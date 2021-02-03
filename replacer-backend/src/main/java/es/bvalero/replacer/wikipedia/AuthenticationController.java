package es.bvalero.replacer.wikipedia;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.ReplacerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Loggable(prepend = true)
@RestController
@RequestMapping("api/authentication")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private WikipediaService wikipediaService;

    @GetMapping(value = "/request-token")
    public RequestToken getRequestToken() throws AuthenticationException {
        return authenticationService.getRequestToken();
    }

    @GetMapping(value = "/access-token")
    public WikipediaUser getAccessToken(
        @RequestParam String requestToken,
        @RequestParam String requestTokenSecret,
        @RequestParam String oauthVerifier
    ) throws AuthenticationException {
        try {
            AccessToken accessToken = authenticationService.getAccessToken(
                requestToken,
                requestTokenSecret,
                oauthVerifier
            );
            String userName = wikipediaService.getLoggedUserName(accessToken);
            boolean admin = wikipediaService.isAdminUser(userName);
            return WikipediaUser.of(userName, admin, accessToken);
        } catch (ReplacerException e) {
            throw new AuthenticationException(e);
        }
    }
}
