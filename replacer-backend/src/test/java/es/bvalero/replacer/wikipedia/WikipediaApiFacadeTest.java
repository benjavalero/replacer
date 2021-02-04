package es.bvalero.replacer.wikipedia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth.OAuth10aService;
import es.bvalero.replacer.ReplacerException;
import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class WikipediaApiFacadeTest {

    @Mock
    private OAuth10aService oAuthService;

    @Spy
    private ObjectMapper jsonMapper;

    @InjectMocks
    private WikipediaApiFacade wikipediaApiFacade;

    @BeforeEach
    public void setUp() {
        wikipediaApiFacade = new WikipediaApiFacade();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetRequestToken() throws Exception {
        OAuth1RequestToken oAuth1RequestToken = new OAuth1RequestToken("A", "B");
        String authorizationUrl = "C";
        Mockito.when(oAuthService.getRequestToken()).thenReturn(oAuth1RequestToken);
        Mockito.when(oAuthService.getAuthorizationUrl(oAuth1RequestToken)).thenReturn(authorizationUrl);

        RequestToken requestToken = RequestToken.of("A", "B", "C");
        Assertions.assertEquals(requestToken, wikipediaApiFacade.getRequestToken());
        Mockito.verify(oAuthService).getRequestToken();
        Mockito.verify(oAuthService).getAuthorizationUrl(oAuth1RequestToken);
    }

    @Test
    void testGetRequestTokenWithException() throws Exception {
        Mockito.when(oAuthService.getRequestToken()).thenThrow(new IOException());

        Assertions.assertThrows(ReplacerException.class, () -> wikipediaApiFacade.getRequestToken());
    }

    @Test
    void testGetAccessToken() throws Exception {
        OAuth1RequestToken requestToken = new OAuth1RequestToken("X", "Y");
        OAuth1AccessToken oAuth1AccessToken = new OAuth1AccessToken("A", "B");
        String oauthVerifier = "C";
        Mockito.when(oAuthService.getAccessToken(requestToken, oauthVerifier)).thenReturn(oAuth1AccessToken);

        AccessToken accessToken = AccessToken.of("A", "B");
        Assertions.assertEquals(accessToken, wikipediaApiFacade.getAccessToken("X", "Y", oauthVerifier));
        Mockito.verify(oAuthService).getAccessToken(requestToken, oauthVerifier);
    }

    @Test
    void testGetAccessTokenWithException() throws Exception {
        Mockito
            .when(oAuthService.getAccessToken(Mockito.any(OAuth1RequestToken.class), Mockito.anyString()))
            .thenThrow(new IOException());

        Assertions.assertThrows(ReplacerException.class, () -> wikipediaApiFacade.getAccessToken("", "", ""));
    }

    @Test
    void testResponseWithErrors() throws Exception {
        // API response
        String textResponse =
            "{\"error\":{\"code\":\"too-many-pageids\",\"info\":\"Too many values supplied for parameter \\\"pageids\\\". The limit is 50.\",\"docref\":\"See https://es.wikipedia.org/w/api.php for API usage. Subscribe to the mediawiki-api-announce mailing list at &lt;https://lists.wikimedia.org/mailman/listinfo/mediawiki-api-announce&gt; for notice of API deprecations and breaking changes.\"},\"servedby\":\"mw1342\"}";
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn(textResponse);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        try {
            wikipediaApiFacade.executeGetRequest(Collections.emptyMap(), WikipediaLanguage.SPANISH);
        } catch (ReplacerException e) {
            Assertions.assertTrue(e.getMessage().startsWith("too-many-pageids"));
        }
    }

    @Test
    void testResponseNotSuccessful() throws Exception {
        // API response
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn("");
        Mockito.when(response.isSuccessful()).thenReturn(false);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        try {
            wikipediaApiFacade.executeGetRequest(Collections.emptyMap(), WikipediaLanguage.SPANISH);
        } catch (ReplacerException e) {
            Assertions.assertTrue(e.getMessage().startsWith("Call not successful"));
        }
    }

    @Test
    void testResponseNull() throws Exception {
        // API response
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn(null);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        try {
            wikipediaApiFacade.executeGetRequest(Collections.emptyMap(), WikipediaLanguage.SPANISH);
        } catch (ReplacerException e) {
            Assertions.assertEquals("ERROR executing OAuth Request", e.getMessage());
        }
    }

    @Test
    void testSignedRequest() throws Exception {
        // API response
        String textResponse =
            "{\"batchcomplete\":true,\"query\":{\"pages\":[{\"pageid\":2209245,\"ns\":4,\"title\":\"Wikipedia:Zona de pruebas/5\",\"revisions\":[{\"timestamp\":\"2019-06-24T21:24:09Z\"}]}],\"tokens\":{\"csrftoken\":\"+\\\\\"}}}";
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn(textResponse);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        Assertions.assertNotNull(
            wikipediaApiFacade.executeSignedPostRequest(
                Collections.emptyMap(),
                WikipediaLanguage.SPANISH,
                AccessToken.of("A", "B")
            )
        );
    }
}
