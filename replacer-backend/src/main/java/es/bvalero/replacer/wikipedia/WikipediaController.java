package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/wikipedia")
public class WikipediaController {

    @Autowired
    private WikipediaService wikipediaService;

    @GetMapping(value = "/username")
    public UserDto getUsername(@RequestParam String token, @RequestParam String tokenSecret)
            throws WikipediaException {
        LOGGER.info("GET Name of the logged user from Wikipedia API. Token: {} / {}", token, tokenSecret);
        OAuth1AccessToken accessToken = new OAuth1AccessToken(token, tokenSecret);

        String username = wikipediaService.identify(accessToken);
        boolean admin = wikipediaService.isAdminUser(username);
        return UserDto.of(username, admin);
    }

}
