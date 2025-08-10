package es.bvalero.replacer.wikipedia.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth.OAuth10aService;
import es.bvalero.replacer.auth.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WikipediaApiHelperTest {

    // Dependency injection
    private OAuth10aService mediaWikiService;
    private ObjectMapper jsonMapper;

    private WikipediaApiHelper wikipediaApiHelper;

    @BeforeEach
    public void setUp() {
        mediaWikiService = mock(OAuth10aService.class);
        jsonMapper = spy(ObjectMapper.class);
        wikipediaApiHelper = new WikipediaApiHelper(mediaWikiService, jsonMapper);
    }

    @Test
    void testResponseWithErrors() throws Exception {
        // API response
        Response response = mock(Response.class);
        String textResponse =
            "{\"error\":{\"code\":\"too-many-pageids\",\"info\":\"Too many values supplied for parameter \\\"pageids\\\". The limit is 50.\",\"docref\":\"See https://es.wikipedia.org/w/api.php for API usage. Subscribe to the mediawiki-api-announce mailing list at &lt;https://lists.wikimedia.org/mailman/listinfo/mediawiki-api-announce&gt; for notice of API deprecations and breaking changes.\"},\"servedby\":\"mw1342\"}";
        when(mediaWikiService.execute(any(OAuthRequest.class))).thenReturn(response);
        when(response.isSuccessful()).thenReturn(true);
        when(response.getBody()).thenReturn(textResponse);

        try {
            WikipediaApiRequest apiRequest = WikipediaApiRequest.builder()
                .verb(WikipediaApiVerb.GET)
                .lang(WikipediaLanguage.getDefault())
                .build();
            wikipediaApiHelper.executeApiRequest(apiRequest);
        } catch (WikipediaException e) {
            assertTrue(e.getMessage().startsWith("too-many-pageids"));
        }
    }

    @Test
    void testResponseNotSuccessful() throws Exception {
        // API response
        Response response = mock(Response.class);
        when(mediaWikiService.execute(any(OAuthRequest.class))).thenReturn(response);
        when(response.isSuccessful()).thenReturn(false);

        WikipediaApiRequest apiRequest = WikipediaApiRequest.builder()
            .verb(WikipediaApiVerb.GET)
            .lang(WikipediaLanguage.getDefault())
            .build();
        assertThrows(WikipediaException.class, () -> wikipediaApiHelper.executeApiRequest(apiRequest));
    }

    @Test
    void testSignedRequest() throws Exception {
        // API response
        Response response = mock(Response.class);
        String textResponse =
            "{\"batchcomplete\":true,\"query\":{\"pages\":[{\"pageid\":2209245,\"ns\":4,\"title\":\"Wikipedia:Zona de pruebas/5\",\"revisions\":[{\"timestamp\":\"2019-06-24T21:24:09Z\"}]}],\"tokens\":{\"csrftoken\":\"+\\\\\"}}}";
        when(mediaWikiService.execute(any(OAuthRequest.class))).thenReturn(response);
        when(response.isSuccessful()).thenReturn(true);
        when(response.getBody()).thenReturn(textResponse);

        WikipediaApiRequest apiRequest = WikipediaApiRequest.builder()
            .verb(WikipediaApiVerb.POST)
            .lang(WikipediaLanguage.getDefault())
            .accessToken(AccessToken.of("A", "B"))
            .build();
        assertNotNull(wikipediaApiHelper.executeApiRequest(apiRequest));
    }
}
