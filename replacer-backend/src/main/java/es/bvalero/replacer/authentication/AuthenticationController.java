package es.bvalero.replacer.authentication;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/authentication")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping(value = "/requestToken")
    public OauthTokenDto getRequestToken() throws AuthenticationException {
        LOGGER.info("GET Request Token from MediaWiki API");
        return convertToDto(authenticationService.getRequestToken());
    }

    @GetMapping(value = "/authorizationUrl")
    public OauthUrlDto getAuthorizationUrl(@RequestParam String token) {
        LOGGER.info("GET Authorization URL from MediaWiki API. Token: {}", token);
        // We don't need the token secret in this call
        OAuth1RequestToken requestToken = new OAuth1RequestToken(token, "");
        return convertToDto(authenticationService.getAuthorizationUrl(requestToken));
    }

    @GetMapping(value = "/accessToken")
    public OauthTokenDto getAccessToken(@RequestParam String token, @RequestParam String tokenSecret,
                                        @RequestParam String verificationToken) throws AuthenticationException {
        LOGGER.info("GET Access Token from MediaWiki API. Token: {} / {} / {}", token, tokenSecret, verificationToken);
        OAuth1RequestToken requestToken = new OAuth1RequestToken(token, tokenSecret);
        return convertToDto(authenticationService.getAccessToken(requestToken, verificationToken));
    }

    private OauthTokenDto convertToDto(OAuth1RequestToken token) {
        return modelMapper.map(token, OauthTokenDto.class);
    }

    private OauthUrlDto convertToDto(String url) {
        return OauthUrlDto.of(url);
    }

    private OauthTokenDto convertToDto(OAuth1AccessToken token) {
        return modelMapper.map(token, OauthTokenDto.class);
    }

}
