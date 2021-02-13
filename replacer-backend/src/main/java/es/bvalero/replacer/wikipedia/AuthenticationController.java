package es.bvalero.replacer.wikipedia;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.ReplacerException;
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
    private WikipediaService wikipediaService;

    @GetMapping(value = "/request-token")
    public RequestToken getRequestToken() throws ReplacerException {
        return wikipediaService.getRequestToken();
    }

    @GetMapping(value = "/logged-user")
    public WikipediaUser getLoggedUser(
        @RequestParam String requestToken,
        @RequestParam String requestTokenSecret,
        @RequestParam String oauthVerifier
    ) throws ReplacerException {
        return wikipediaService.getLoggedUser(requestToken, requestTokenSecret, oauthVerifier);
    }
}
