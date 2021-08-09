package es.bvalero.replacer.wikipedia.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.OAuthService;
import es.bvalero.replacer.wikipedia.OAuthToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class WikipediaRequestHelperTest {

    @Mock
    private OAuthService oAuthService;

    @Spy
    private ObjectMapper jsonMapper;

    @InjectMocks
    private WikipediaRequestHelper wikipediaRequestHelper;

    @BeforeEach
    public void setUp() {
        wikipediaRequestHelper = new WikipediaRequestHelper();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testResponseWithErrors() throws Exception {
        // API response
        String textResponse =
            "{\"error\":{\"code\":\"too-many-pageids\",\"info\":\"Too many values supplied for parameter \\\"pageids\\\". The limit is 50.\",\"docref\":\"See https://es.wikipedia.org/w/api.php for API usage. Subscribe to the mediawiki-api-announce mailing list at &lt;https://lists.wikimedia.org/mailman/listinfo/mediawiki-api-announce&gt; for notice of API deprecations and breaking changes.\"},\"servedby\":\"mw1342\"}";
        Mockito
            .when(oAuthService.executeRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap()))
            .thenReturn(textResponse);

        try {
            WikipediaApiRequest apiRequest = WikipediaApiRequest
                .builder()
                .verb(WikipediaApiRequestVerb.GET)
                .lang(WikipediaLanguage.getDefault())
                .build();
            wikipediaRequestHelper.executeApiRequest(apiRequest);
        } catch (ReplacerException e) {
            Assertions.assertTrue(e.getMessage().startsWith("too-many-pageids"));
        }
    }

    @Test
    void testResponseNotSuccessful() throws Exception {
        // API response
        Mockito
            .when(oAuthService.executeRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap()))
            .thenThrow(ReplacerException.class);

        WikipediaApiRequest apiRequest = WikipediaApiRequest
            .builder()
            .verb(WikipediaApiRequestVerb.GET)
            .lang(WikipediaLanguage.getDefault())
            .build();
        Assertions.assertThrows(ReplacerException.class, () -> wikipediaRequestHelper.executeApiRequest(apiRequest));
    }

    @Test
    void testSignedRequest() throws Exception {
        // API response
        String textResponse =
            "{\"batchcomplete\":true,\"query\":{\"pages\":[{\"pageid\":2209245,\"ns\":4,\"title\":\"Wikipedia:Zona de pruebas/5\",\"revisions\":[{\"timestamp\":\"2019-06-24T21:24:09Z\"}]}],\"tokens\":{\"csrftoken\":\"+\\\\\"}}}";
        Mockito
            .when(
                oAuthService.executeSignedRequest(
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.anyMap(),
                    Mockito.any(OAuthToken.class)
                )
            )
            .thenReturn(textResponse);

        WikipediaApiRequest apiRequest = WikipediaApiRequest
            .builder()
            .verb(WikipediaApiRequestVerb.POST)
            .lang(WikipediaLanguage.getDefault())
            .accessToken(OAuthToken.of("A", "B"))
            .build();
        Assertions.assertNotNull(wikipediaRequestHelper.executeApiRequest(apiRequest));
    }
}
