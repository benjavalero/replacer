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
import java.util.List;

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
        String textResponse = "{\"batchcomplete\":true,\"query\":{\"pages\":[{\"pageid\":2209245,\"ns\":4,\"title\":\"Wikipedia:Zona de pruebas/5\",\"revisions\":[{\"timestamp\":\"2019-06-24T21:24:09Z\"}]}],\"tokens\":{\"csrftoken\":\"+\\\\\"}}}";
        Mockito.when(authenticationService.executeOAuthRequest(Mockito.anyString(), Mockito.anyMap(),
                Mockito.anyBoolean(), Mockito.nullable(OAuth1AccessToken.class))).thenReturn(textResponse);

        // We pass a null access token to retrieve an anonymous edit token
        EditToken editToken = wikipediaService.getEditToken(2209245, new OAuth1AccessToken("", ""));
        Assert.assertNotNull(editToken.getCsrftoken());
        Assert.assertEquals("+\\", editToken.getCsrftoken());
        Assert.assertEquals("2019-06-24T21:24:09Z", editToken.getTimestamp());
    }

    @Test(expected = WikipediaException.class)
    public void testGetEditTokenBadAuthentication() throws AuthenticationException, WikipediaException {
        // API response
        String textResponse = "{\"error\":{\"code\":\"mwoauth-invalid-authorization\",\"info\":\"The authorization headers in your request are not valid: No approved grant was found for that authorization token.\",\"docref\":\"See https://es.wikipedia.org/w/api.php for API usage. Subscribe to the mediawiki-api-announce mailing list at &lt;https://lists.wikimedia.org/mailman/listinfo/mediawiki-api-announce&gt; for notice of API deprecations and breaking changes.\"},\"servedby\":\"mw1235\"}";
        Mockito.when(authenticationService.executeOAuthRequest(Mockito.anyString(), Mockito.anyMap(),
                Mockito.anyBoolean(), Mockito.nullable(OAuth1AccessToken.class))).thenReturn(textResponse);

        // We pass a null access token to retrieve an anonymous edit token
        wikipediaService.getEditToken(1, new OAuth1AccessToken("", ""));
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

        List<WikipediaPage> pages = wikipediaService.getPagesByIds(Arrays.asList(6219990, 6903884));
        Assert.assertNotNull(pages);
        Assert.assertEquals(2, pages.size());
        Assert.assertTrue(pages.stream().anyMatch(page -> page.getId() == 6219990));
        Assert.assertTrue(pages.stream().filter(page -> page.getId() == 6219990).findAny().orElseThrow(WikipediaException::new).getContent().contains("Orihuela"));
        Assert.assertTrue(pages.stream().anyMatch(page -> page.getId() == 6903884));
        Assert.assertTrue(pages.stream().filter(page -> page.getId() == 6903884).findAny().orElseThrow(WikipediaException::new).getContent().contains("Pais Vasco"));
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

    @Test
    public void testGetPageIdsByStringMatch() throws AuthenticationException, WikipediaException {
        // API response
        String textResponse = "{\"batchcomplete\":\"\",\"continue\":{\"sroffset\":10,\"continue\":\"-||\"},\"query\":{\"search\":[{\"ns\":0,\"title\":\"Belanova\",\"pageid\":297896},{\"ns\":0,\"title\":\"Wil Hartog\",\"pageid\":7694956},{\"ns\":0,\"title\":\"Compuesto químico\",\"pageid\":10547},{\"ns\":0,\"title\":\"Aun así te vas\",\"pageid\":2460037},{\"ns\":0,\"title\":\"Educación\",\"pageid\":975},{\"ns\":0,\"title\":\"Abolicionismo\",\"pageid\":173068},{\"ns\":0,\"title\":\"Canaán\",\"pageid\":718871},{\"ns\":0,\"title\":\"Coahuila de Zaragoza\",\"pageid\":724588},{\"ns\":0,\"title\":\"Filosofía\",\"pageid\":689592},{\"ns\":0,\"title\":\"Cárites\",\"pageid\":71433}]}}";
        Mockito.when(authenticationService.executeOAuthRequest(Mockito.anyString(), Mockito.anyMap(),
                Mockito.anyBoolean(), Mockito.nullable(OAuth1AccessToken.class))).thenReturn(textResponse);

        List<Integer> pageIds = wikipediaService.getPageIdsByStringMatch("");
        Assert.assertEquals(10, pageIds.size());
    }

    @Test
    public void testGetPageIdsByStringMatchWithNoResults() throws AuthenticationException, WikipediaException {
        // API response
        String textResponse = "{\"batchcomplete\":\"\",\"query\":{\"search\":[]}}";
        Mockito.when(authenticationService.executeOAuthRequest(Mockito.anyString(), Mockito.anyMap(),
                Mockito.anyBoolean(), Mockito.nullable(OAuth1AccessToken.class))).thenReturn(textResponse);

        List<Integer> pageIds = wikipediaService.getPageIdsByStringMatch("");
        Assert.assertTrue(pageIds.isEmpty());
    }

}
