package es.bvalero.replacer.wikipedia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class WikipediaRequestServiceTest {

    @Mock
    private OAuth10aService oAuthService;

    @Spy
    private ObjectMapper jsonMapper;

    @InjectMocks
    private WikipediaRequestService wikipediaRequestService;

    @Before
    public void setUp() {
        wikipediaRequestService = new WikipediaRequestService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testResponseWithErrors() throws IOException, ExecutionException, InterruptedException {
        // API response
        String textResponse = "{\"error\":{\"code\":\"too-many-pageids\",\"info\":\"Too many values supplied for parameter \\\"pageids\\\". The limit is 50.\",\"docref\":\"See https://es.wikipedia.org/w/api.php for API usage. Subscribe to the mediawiki-api-announce mailing list at &lt;https://lists.wikimedia.org/mailman/listinfo/mediawiki-api-announce&gt; for notice of API deprecations and breaking changes.\"},\"servedby\":\"mw1342\"}";
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn(textResponse);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        try {
            wikipediaRequestService.executeGetRequest(Collections.emptyMap());
        } catch (WikipediaException e) {
            Assert.assertTrue(e.getMessage().startsWith("too-many-pageids"));
        }
    }

    @Test
    public void testResponseNotSuccessful() throws IOException, ExecutionException, InterruptedException {
        // API response
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn("");
        Mockito.when(response.isSuccessful()).thenReturn(false);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        try {
            wikipediaRequestService.executeGetRequest(Collections.emptyMap());
        } catch (WikipediaException e) {
            Assert.assertTrue(e.getMessage().startsWith("Call not successful"));
        }
    }

    @Test
    public void testResponseNull() throws IOException, ExecutionException, InterruptedException {
        // API response
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn(null);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        try {
            wikipediaRequestService.executeGetRequest(Collections.emptyMap());
        } catch (WikipediaException e) {
            Assert.assertEquals("ERROR executing OAuth Request", e.getMessage());
        }
    }

    @Test
    public void testSignedRequest() throws WikipediaException, ExecutionException, InterruptedException, IOException {
        // API response
        String textResponse = "{\"batchcomplete\":true,\"query\":{\"pages\":[{\"pageid\":2209245,\"ns\":4,\"title\":\"Wikipedia:Zona de pruebas/5\",\"revisions\":[{\"timestamp\":\"2019-06-24T21:24:09Z\"}]}],\"tokens\":{\"csrftoken\":\"+\\\\\"}}}";
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn(textResponse);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        Assert.assertNotNull(wikipediaRequestService.executeSignedPostRequest(
                Collections.emptyMap(), new OAuth1AccessToken("A", "B")));
    }

}
