package es.bvalero.replacer.wikipedia.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth.OAuth10aService;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.OAuthToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class WikipediaApiRequestHelperTest {

    @Mock
    private OAuth10aService mediaWikiApiService;

    @Spy
    private ObjectMapper jsonMapper;

    @InjectMocks
    private WikipediaApiRequestHelper wikipediaApiRequestHelper;

    @BeforeEach
    public void setUp() {
        wikipediaApiRequestHelper = new WikipediaApiRequestHelper();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testResponseWithErrors() throws Exception {
        // API response
        Response response = Mockito.mock(Response.class);
        String textResponse =
            "{\"error\":{\"code\":\"too-many-pageids\",\"info\":\"Too many values supplied for parameter \\\"pageids\\\". The limit is 50.\",\"docref\":\"See https://es.wikipedia.org/w/api.php for API usage. Subscribe to the mediawiki-api-announce mailing list at &lt;https://lists.wikimedia.org/mailman/listinfo/mediawiki-api-announce&gt; for notice of API deprecations and breaking changes.\"},\"servedby\":\"mw1342\"}";
        Mockito.when(mediaWikiApiService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(response.getBody()).thenReturn(textResponse);

        try {
            WikipediaApiRequest apiRequest = WikipediaApiRequest
                .builder()
                .verb(WikipediaApiRequestVerb.GET)
                .lang(WikipediaLanguage.getDefault())
                .build();
            wikipediaApiRequestHelper.executeApiRequest(apiRequest);
        } catch (ReplacerException e) {
            Assertions.assertTrue(e.getMessage().startsWith("too-many-pageids"));
        }
    }

    @Test
    void testResponseNotSuccessful() throws Exception {
        // API response
        Response response = Mockito.mock(Response.class);
        Mockito.when(mediaWikiApiService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);
        Mockito.when(response.isSuccessful()).thenReturn(false);

        WikipediaApiRequest apiRequest = WikipediaApiRequest
            .builder()
            .verb(WikipediaApiRequestVerb.GET)
            .lang(WikipediaLanguage.getDefault())
            .build();
        Assertions.assertThrows(ReplacerException.class, () -> wikipediaApiRequestHelper.executeApiRequest(apiRequest));
    }

    @Test
    void testSignedRequest() throws Exception {
        // API response
        Response response = Mockito.mock(Response.class);
        String textResponse =
            "{\"batchcomplete\":true,\"query\":{\"pages\":[{\"pageid\":2209245,\"ns\":4,\"title\":\"Wikipedia:Zona de pruebas/5\",\"revisions\":[{\"timestamp\":\"2019-06-24T21:24:09Z\"}]}],\"tokens\":{\"csrftoken\":\"+\\\\\"}}}";
        Mockito.when(mediaWikiApiService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(response.getBody()).thenReturn(textResponse);

        WikipediaApiRequest apiRequest = WikipediaApiRequest
            .builder()
            .verb(WikipediaApiRequestVerb.POST)
            .lang(WikipediaLanguage.getDefault())
            .accessToken(OAuthToken.of("A", "B"))
            .build();
        Assertions.assertNotNull(wikipediaApiRequestHelper.executeApiRequest(apiRequest));
    }
}
