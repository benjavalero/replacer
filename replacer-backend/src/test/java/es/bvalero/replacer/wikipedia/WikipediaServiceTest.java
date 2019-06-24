package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.authentication.AuthenticationException;
import es.bvalero.replacer.authentication.AuthenticationService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class WikipediaServiceTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private WikipediaServiceImpl wikipediaService;

    @Before
    public void setUp() {
        wikipediaService = new WikipediaServiceImpl();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetEditToken() throws AuthenticationException, WikipediaException {
        // API response
        String textResponse = "{\"batchcomplete\":true,\"query\":{\"tokens\":{\"csrftoken\":\"+\\\\\"}}}";
        Mockito.when(authenticationService.executeOAuthRequest(Mockito.anyString(), Mockito.anyMap(),
                Mockito.anyBoolean(), Mockito.nullable(OAuth1AccessToken.class))).thenReturn(textResponse);

        // We pass a null access token to retrieve an anonymous edit token
        String editToken = wikipediaService.getEditToken(new OAuth1AccessToken("", ""));
        Assert.assertNotNull(editToken);
        Assert.assertTrue(editToken.endsWith("+\\"));
    }

    @Test(expected = WikipediaException.class)
    public void testGetEditTokenBadAuthentication() throws AuthenticationException, WikipediaException {
        // API response
        String textResponse = "{\"error\":{\"code\":\"mwoauth-invalid-authorization\",\"info\":\"The authorization headers in your request are not valid: No approved grant was found for that authorization token.\",\"docref\":\"See https://es.wikipedia.org/w/api.php for API usage. Subscribe to the mediawiki-api-announce mailing list at &lt;https://lists.wikimedia.org/mailman/listinfo/mediawiki-api-announce&gt; for notice of API deprecations and breaking changes.\"},\"servedby\":\"mw1235\"}";
        Mockito.when(authenticationService.executeOAuthRequest(Mockito.anyString(), Mockito.anyMap(),
                Mockito.anyBoolean(), Mockito.nullable(OAuth1AccessToken.class))).thenReturn(textResponse);

        // We pass a null access token to retrieve an anonymous edit token
        wikipediaService.getEditToken(new OAuth1AccessToken("", ""));
    }

    @Test
    public void testGetPageContent() throws AuthenticationException, WikipediaException {
        // API response
        String textResponse = "{\"batchcomplete\":true,\"curtimestamp\": \"2019-06-13T10:41:02Z\",\"query\":{\"pages\":[{\"pageid\":6219990,\"ns\":2,\"title\":\"Usuario:Benjavalero\",\"revisions\":[{\"timestamp\": \"2016-02-26T21:48:59Z\",\"slots\":{\"main\":{\"contentmodel\":\"wikitext\",\"contentformat\":\"text/x-wiki\",\"content\":\"Soy de [[Orihuela]]\"}}}]}]}}";
        Mockito.when(authenticationService.executeOAuthRequest(Mockito.anyString(), Mockito.anyMap(),
                Mockito.anyBoolean(), Mockito.nullable(OAuth1AccessToken.class))).thenReturn(textResponse);

        String title = "Usuario:Benjavalero";
        WikipediaPage page = wikipediaService.getPageByTitle(title)
                .orElseThrow(WikipediaException::new);
        Assert.assertNotNull(page);
        Assert.assertEquals(6219990, page.getId());
        Assert.assertEquals(title, page.getTitle());
        Assert.assertEquals(WikipediaNamespace.USER, page.getNamespace());
        Assert.assertTrue(page.getLastUpdate().getYear() >= 2016);
        Assert.assertTrue(page.getContent().contains("Orihuela"));
    }

    @Test
    public void testGetPagesContent() throws AuthenticationException, WikipediaException {
        // API response
        String textResponse = "{\"batchcomplete\":true,\"curtimestamp\": \"2019-06-13T10:41:02Z\",\"query\":{\"pages\":[{\"pageid\":6219990,\"ns\":2,\"title\":\"Usuario:Benjavalero\",\"revisions\":[{\"timestamp\": \"2016-02-26T21:48:59Z\",\"slots\":{\"main\":{\"contentmodel\":\"wikitext\",\"contentformat\":\"text/x-wiki\",\"content\":\"Soy de [[Orihuela]]\"}}}]},{\"pageid\":6903884,\"ns\":2,\"title\":\"Usuario:Benjavalero/Taller\",\"revisions\":[{\"timestamp\": \"2016-02-26T21:48:59Z\",\"slots\":{\"main\":{\"contentmodel\":\"wikitext\",\"contentformat\":\"text/x-wiki\",\"content\":\"Enlace a [[Pais Vasco]].\"}}}]}]}}";
        Mockito.when(authenticationService.executeOAuthRequest(Mockito.anyString(), Mockito.anyMap(),
                Mockito.anyBoolean(), Mockito.nullable(OAuth1AccessToken.class))).thenReturn(textResponse);

        Map<Integer, WikipediaPage> pages = wikipediaService.getPagesByIds(Arrays.asList(6219990, 6903884));
        Assert.assertNotNull(pages);
        Assert.assertEquals(2, pages.size());
        Assert.assertTrue(pages.containsKey(6219990));
        Assert.assertTrue(pages.get(6219990).getContent().contains("Orihuela"));
        Assert.assertTrue(pages.containsKey(6903884));
        Assert.assertTrue(pages.get(6903884).getContent().contains("Pais Vasco"));
    }

    @Test(expected = WikipediaException.class)
    public void testGetPagesContentWithErrors() throws AuthenticationException, WikipediaException {
        // API response
        String textResponse = "{\"error\":{\"code\":\"too-many-pageids\",\"info\":\"Too many values supplied for parameter \\\"pageids\\\". The limit is 50.\",\"docref\":\"See https://es.wikipedia.org/w/api.php for API usage. Subscribe to the mediawiki-api-announce mailing list at &lt;https://lists.wikimedia.org/mailman/listinfo/mediawiki-api-announce&gt; for notice of API deprecations and breaking changes.\"},\"servedby\":\"mw1342\"}";
        Mockito.when(authenticationService.executeOAuthRequest(Mockito.anyString(), Mockito.anyMap(),
                Mockito.anyBoolean(), Mockito.nullable(OAuth1AccessToken.class))).thenReturn(textResponse);

        wikipediaService.getPagesByIds(Collections.singletonList(6219990));
    }

    @Test
    public void testGetPageContentUnavailable() throws AuthenticationException, WikipediaException {
        // API response
        String textResponse = "{\"batchcomplete\":true,\"curtimestamp\": \"2019-06-13T10:41:02Z\",\"query\":{\"pages\":[{\"ns\":2,\"title\":\"Usuario:Benjavaleroxx\",\"missing\":true}]}}";
        Mockito.when(authenticationService.executeOAuthRequest(Mockito.anyString(), Mockito.anyMap(),
                Mockito.anyBoolean(), Mockito.nullable(OAuth1AccessToken.class))).thenReturn(textResponse);

        Assert.assertFalse(wikipediaService.getPageByTitle("Usuario:Benjavaleroxx").isPresent());
    }

}
