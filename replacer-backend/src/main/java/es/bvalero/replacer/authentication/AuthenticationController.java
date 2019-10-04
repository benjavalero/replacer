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
    public RequestTokenDto getRequestToken() throws AuthenticationException {
        LOGGER.info("GET Request Token from MediaWiki API");
        OAuth1RequestToken requestToken = authenticationService.getRequestToken();
        String authorizationUrl = authenticationService.getAuthorizationUrl(requestToken);
        return convertToDto(requestToken, authorizationUrl);
    }

    @PostMapping(value = "/accessToken")
    public AccessTokenDto getAccessToken(@RequestBody VerificationTokenDto verificationTokenDto)
            throws AuthenticationException {
        LOGGER.info("GET Access Token from MediaWiki API: {}", verificationTokenDto);
        return convertToDto(authenticationService.getAccessToken(
                convertToEntity(verificationTokenDto.getRequestToken()),
                verificationTokenDto.getVerificationToken()));
    }

    private RequestTokenDto convertToDto(OAuth1RequestToken requestToken, String authorizationUrl) {
        RequestTokenDto requestTokenDto = modelMapper.map(requestToken, RequestTokenDto.class);
        requestTokenDto.setUrl(authorizationUrl);
        return requestTokenDto;
    }

    private OAuth1RequestToken convertToEntity(RequestTokenDto requestTokenDto) {
        return new OAuth1RequestToken(requestTokenDto.getToken(), requestTokenDto.getTokenSecret());
    }

    private AccessTokenDto convertToDto(OAuth1AccessToken token) {
        return modelMapper.map(token, AccessTokenDto.class);
    }

}
