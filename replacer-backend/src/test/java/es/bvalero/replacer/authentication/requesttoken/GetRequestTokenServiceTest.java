package es.bvalero.replacer.authentication.requesttoken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.authentication.oauth.OAuthService;
import es.bvalero.replacer.authentication.oauth.RequestToken;
import es.bvalero.replacer.common.exception.ReplacerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class GetRequestTokenServiceTest {

    @Mock
    private OAuthService oAuthService;

    @InjectMocks
    private GetRequestTokenService getRequestTokenService;

    @BeforeEach
    public void setUp() {
        getRequestTokenService = new GetRequestTokenService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetRequestToken() throws ReplacerException {
        RequestToken requestToken = RequestToken.of("R", "S");
        when(oAuthService.getRequestToken()).thenReturn(requestToken);
        String authorizationUrl = "Z";
        when(oAuthService.getAuthorizationUrl(requestToken)).thenReturn(authorizationUrl);

        GetRequestTokenResponse response = getRequestTokenService.get();

        assertEquals(requestToken.getToken(), response.getToken());
        assertEquals(requestToken.getTokenSecret(), response.getTokenSecret());
        assertEquals(authorizationUrl, response.getAuthorizationUrl());

        verify(oAuthService).getRequestToken();
        verify(oAuthService).getAuthorizationUrl(requestToken);
    }
}
