package es.bvalero.replacer.wikipedia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth.OAuth10aService;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class WikipediaApiFacadeTest {

    @Mock
    private OAuth10aService oAuth10aService;

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
    void testGetAccessToken() throws Exception {
        OAuth1RequestToken requestToken = new OAuth1RequestToken("X", "Y");
        OAuth1AccessToken oAuth1AccessToken = new OAuth1AccessToken("A", "B");
        String oauthVerifier = "C";
        Mockito.when(oAuth10aService.getAccessToken(requestToken, oauthVerifier)).thenReturn(oAuth1AccessToken);

        OAuthToken accessToken = OAuthToken.of("A", "B");
        Assertions.assertEquals(accessToken, wikipediaApiFacade.getAccessToken("X", "Y", oauthVerifier));
        Mockito.verify(oAuth10aService).getAccessToken(requestToken, oauthVerifier);
    }

    @Test
    void testGetAccessTokenWithException() throws Exception {
        Mockito
            .when(oAuth10aService.getAccessToken(Mockito.any(OAuth1RequestToken.class), Mockito.anyString()))
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
        Mockito.when(oAuth10aService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

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
        Mockito.when(oAuth10aService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

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
        Mockito.when(oAuth10aService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

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
        Mockito.when(oAuth10aService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        Assertions.assertNotNull(
            wikipediaApiFacade.executeSignedPostRequest(
                Collections.emptyMap(),
                WikipediaLanguage.SPANISH,
                OAuthToken.of("A", "B")
            )
        );
    }
}
