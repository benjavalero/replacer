package es.bvalero.replacer.authentication;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/authentication")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping(value = "/requestToken")
    public RequestToken getRequestToken() throws AuthenticationException {
        LOGGER.info("GET Request Token from MediaWiki API");
        OAuth1RequestToken requestToken = authenticationService.getRequestToken();
        String authorizationUrl = authenticationService.getAuthorizationUrl(requestToken);
        return convertToDto(requestToken, authorizationUrl);
    }

    @PostMapping(value = "/accessToken")
    public AccessToken getAccessToken(@RequestBody VerificationToken verificationToken)
            throws AuthenticationException {
        LOGGER.info("GET Access Token from MediaWiki API: {}", verificationToken);
        return convertToDto(authenticationService.getAccessToken(
                convertToEntity(verificationToken.getRequestToken()),
                verificationToken.getToken()));
    }

    private RequestToken convertToDto(OAuth1RequestToken oAuth1RequestToken, String authorizationUrl) {
        RequestToken requestToken = modelMapper.map(oAuth1RequestToken, RequestToken.class);
        requestToken.setUrl(authorizationUrl);
        return requestToken;
    }

    private OAuth1RequestToken convertToEntity(RequestToken requestToken) {
        return new OAuth1RequestToken(requestToken.getToken(), requestToken.getTokenSecret());
    }

    private AccessToken convertToDto(OAuth1AccessToken token) {
        return modelMapper.map(token, AccessToken.class);
    }

}
