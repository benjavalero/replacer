package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth.OAuthService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class WikipediaRequestServiceTest {

    @Mock
    private OAuthService oAuthService;

    @InjectMocks
    private WikipediaRequestService wikipediaRequestService;

    @Before
    public void setUp() {
        wikipediaRequestService = new WikipediaRequestService();
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = WikipediaException.class)
    public void testGetPagesContentWithErrors() throws WikipediaException, ExecutionException, InterruptedException, IOException {
        // API response
        String textResponse = "{\"error\":{\"code\":\"too-many-pageids\",\"info\":\"Too many values supplied for parameter \\\"pageids\\\". The limit is 50.\",\"docref\":\"See https://es.wikipedia.org/w/api.php for API usage. Subscribe to the mediawiki-api-announce mailing list at &lt;https://lists.wikimedia.org/mailman/listinfo/mediawiki-api-announce&gt; for notice of API deprecations and breaking changes.\"},\"servedby\":\"mw1342\"}";
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn(textResponse);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        wikipediaRequestService.executeGetRequest(Collections.emptyMap());
    }

    @Test(expected = WikipediaException.class)
    public void testGetEditTokenBadAuthentication() throws WikipediaException, ExecutionException, InterruptedException, IOException {
        // API response
        String textResponse = "{\"error\":{\"code\":\"mwoauth-invalid-authorization\",\"info\":\"The authorization headers in your request are not valid: No approved grant was found for that authorization token.\",\"docref\":\"See https://es.wikipedia.org/w/api.php for API usage. Subscribe to the mediawiki-api-announce mailing list at &lt;https://lists.wikimedia.org/mailman/listinfo/mediawiki-api-announce&gt; for notice of API deprecations and breaking changes.\"},\"servedby\":\"mw1235\"}";
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn(textResponse);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        // We pass a null access token to retrieve an anonymous edit token
        wikipediaRequestService.executeSignedPostRequest(Collections.emptyMap(), new OAuth1AccessToken("", ""));
    }

}
