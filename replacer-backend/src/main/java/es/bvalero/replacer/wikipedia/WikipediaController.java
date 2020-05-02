package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.authentication.AccessToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/wikipedia")
public class WikipediaController {

    @Autowired
    private WikipediaService wikipediaService;

    @PostMapping(value = "/username")
    public WikipediaUser getUsername(@RequestBody AccessToken accessToken) throws ReplacerException {
        LOGGER.info("GET Name of the logged user from Wikipedia API: {}", accessToken);
        // TODO: Receive language as a parameter
        String userName = wikipediaService.getLoggedUserName(convertToEntity(accessToken), WikipediaLanguage.SPANISH);
        boolean admin = wikipediaService.isAdminUser(userName);
        WikipediaUser user = WikipediaUser.of(userName, admin);
        LOGGER.info("RETURN Name of the logged user: {}", user);
        return user;
    }

    private OAuth1AccessToken convertToEntity(AccessToken accessToken) {
        return new OAuth1AccessToken(accessToken.getToken(), accessToken.getTokenSecret());
    }

}
