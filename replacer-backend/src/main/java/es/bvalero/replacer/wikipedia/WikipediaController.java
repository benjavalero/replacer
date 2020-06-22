package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.ReplacerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api")
public class WikipediaController {
    @Autowired
    private WikipediaService wikipediaService;

    @GetMapping(value = "/authentication/user")
    public WikipediaUser getUsername(
        @RequestParam String accessToken,
        @RequestParam String accessTokenSecret,
        @RequestParam(required = false) WikipediaLanguage lang
    )
        throws ReplacerException {
        LOGGER.info("GET Logged user from Wikipedia API: {}", accessToken);
        if (lang == null) {
            // Default value
            lang = WikipediaLanguage.SPANISH;
        }
        OAuth1AccessToken oAuth1AccessToken = new OAuth1AccessToken(accessToken, accessTokenSecret);
        String userName = wikipediaService.getLoggedUserName(oAuth1AccessToken, lang);
        boolean admin = wikipediaService.isAdminUser(userName);
        WikipediaUser user = WikipediaUser.of(userName, admin);
        LOGGER.info("RETURN Logged user: {}", user);
        return user;
    }
}
