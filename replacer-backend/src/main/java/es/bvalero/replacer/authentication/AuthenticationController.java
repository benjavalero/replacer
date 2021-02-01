package es.bvalero.replacer.authentication;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.modelmapper.ModelMapper;
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

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping(value = "/request-token")
    public RequestToken getRequestToken() throws AuthenticationException {
        OAuth1RequestToken requestToken = authenticationService.getRequestToken();
        String authorizationUrl = authenticationService.getAuthorizationUrl(requestToken);
        return convertToDto(requestToken, authorizationUrl);
    }

    @GetMapping(value = "/access-token")
    public WikipediaUser getAccessToken(
        @RequestParam String requestToken,
        @RequestParam String requestTokenSecret,
        @RequestParam String oauthVerifier
    ) throws AuthenticationException {
        OAuth1RequestToken oAuth1RequestToken = new OAuth1RequestToken(requestToken, requestTokenSecret);
        OAuth1AccessToken oAuth1AccessToken = authenticationService.getAccessToken(oAuth1RequestToken, oauthVerifier);

        try {
            String userName = wikipediaService.getLoggedUserName(oAuth1AccessToken);
            boolean admin = wikipediaService.isAdminUser(userName);
            AccessToken accessToken = convertToDto(oAuth1AccessToken);
            return WikipediaUser.of(userName, admin, accessToken);
        } catch (ReplacerException e) {
            throw new AuthenticationException(e);
        }
    }

    private RequestToken convertToDto(OAuth1RequestToken oAuth1RequestToken, String authorizationUrl) {
        return RequestToken.of(oAuth1RequestToken.getToken(), oAuth1RequestToken.getTokenSecret(), authorizationUrl);
    }

    private AccessToken convertToDto(OAuth1AccessToken token) {
        return modelMapper.map(token, AccessToken.class);
    }
}
