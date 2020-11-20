package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.ReplacerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Loggable(prepend = true)
@RestController
@RequestMapping("api")
public class WikipediaController {
    @Autowired
    private WikipediaService wikipediaService;

    @GetMapping(value = "/authentication/user")
    public WikipediaUser getUsername(
        @RequestParam String accessToken,
        @RequestParam String accessTokenSecret,
        @RequestParam WikipediaLanguage lang
    )
        throws ReplacerException {
        OAuth1AccessToken oAuth1AccessToken = new OAuth1AccessToken(accessToken, accessTokenSecret);
        String userName = wikipediaService.getLoggedUserName(oAuth1AccessToken, lang);
        boolean admin = wikipediaService.isAdminUser(userName);
        return WikipediaUser.of(userName, admin);
    }
}
