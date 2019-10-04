package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class WikipediaServiceTest {

    @Mock
    private OAuth10aService oAuthService;

    @InjectMocks
    private WikipediaServiceImpl wikipediaService;

    @Before
    public void setUp() {
        wikipediaService = new WikipediaServiceImpl();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetEditToken() throws WikipediaException, InterruptedException, ExecutionException, IOException {
        // API response
        String textResponse = "{\"batchcomplete\":true,\"query\":{\"pages\":[{\"pageid\":2209245,\"ns\":4,\"title\":\"Wikipedia:Zona de pruebas/5\",\"revisions\":[{\"timestamp\":\"2019-06-24T21:24:09Z\"}]}],\"tokens\":{\"csrftoken\":\"+\\\\\"}}}";
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn(textResponse);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        // We pass a null access token to retrieve an anonymous edit token
        EditToken editToken = wikipediaService.getEditToken(2209245, new OAuth1AccessToken("", ""));
        Assert.assertNotNull(editToken.getCsrftoken());
        Assert.assertEquals("+\\", editToken.getCsrftoken());
        Assert.assertEquals("2019-06-24T21:24:09Z", editToken.getTimestamp());
    }

    @Test(expected = WikipediaException.class)
    public void testGetEditTokenBadAuthentication() throws WikipediaException, InterruptedException, ExecutionException, IOException {
        // API response
        String textResponse = "{\"error\":{\"code\":\"mwoauth-invalid-authorization\",\"info\":\"The authorization headers in your request are not valid: No approved grant was found for that authorization token.\",\"docref\":\"See https://es.wikipedia.org/w/api.php for API usage. Subscribe to the mediawiki-api-announce mailing list at &lt;https://lists.wikimedia.org/mailman/listinfo/mediawiki-api-announce&gt; for notice of API deprecations and breaking changes.\"},\"servedby\":\"mw1235\"}";
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn(textResponse);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        // We pass a null access token to retrieve an anonymous edit token
        wikipediaService.getEditToken(1, new OAuth1AccessToken("", ""));
    }

    @Test
    public void testGetPageContentByTitle() throws WikipediaException, InterruptedException, ExecutionException, IOException {
        // API response
        String textResponse = "{\"batchcomplete\":true,\"curtimestamp\": \"2019-06-13T10:41:02Z\",\"query\":{\"pages\":[{\"pageid\":6219990,\"ns\":2,\"title\":\"Usuario:Benjavalero\",\"revisions\":[{\"timestamp\": \"2016-02-26T21:48:59Z\",\"slots\":{\"main\":{\"contentmodel\":\"wikitext\",\"contentformat\":\"text/x-wiki\",\"content\":\"Soy de [[Orihuela]]\"}}}]}]}}";
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn(textResponse);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        int pageId = 6219990;
        String title = "Usuario:Benjavalero";
        WikipediaPage page = wikipediaService.getPageByTitle(title)
                .orElseThrow(WikipediaException::new);
        Assert.assertNotNull(page);
        Assert.assertEquals(pageId, page.getId());
        Assert.assertEquals(title, page.getTitle());
        Assert.assertEquals(WikipediaNamespace.USER, page.getNamespace());
        Assert.assertTrue(page.getLastUpdate().getYear() >= 2016);
        Assert.assertTrue(page.getContent().contains("Orihuela"));
    }

    @Test
    public void testGetPageContentById() throws WikipediaException, InterruptedException, ExecutionException, IOException {
        // API response
        String textResponse = "{\"batchcomplete\":true,\"curtimestamp\": \"2019-06-13T10:41:02Z\",\"query\":{\"pages\":[{\"pageid\":6219990,\"ns\":2,\"title\":\"Usuario:Benjavalero\",\"revisions\":[{\"timestamp\": \"2016-02-26T21:48:59Z\",\"slots\":{\"main\":{\"contentmodel\":\"wikitext\",\"contentformat\":\"text/x-wiki\",\"content\":\"Soy de [[Orihuela]]\"}}}]}]}}";
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn(textResponse);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        int pageId = 6219990;
        String title = "Usuario:Benjavalero";
        WikipediaPage page = wikipediaService.getPageById(pageId)
                .orElseThrow(WikipediaException::new);
        Assert.assertNotNull(page);
        Assert.assertEquals(pageId, page.getId());
        Assert.assertEquals(title, page.getTitle());
        Assert.assertEquals(WikipediaNamespace.USER, page.getNamespace());
        Assert.assertTrue(page.getLastUpdate().getYear() >= 2016);
        Assert.assertTrue(page.getContent().contains("Orihuela"));
    }

    @Test
    public void testGetPagesContent() throws WikipediaException, InterruptedException, ExecutionException, IOException {
        // API response
        String textResponse = "{\"batchcomplete\":true,\"curtimestamp\": \"2019-06-13T10:41:02Z\",\"query\":{\"pages\":[{\"pageid\":6219990,\"ns\":2,\"title\":\"Usuario:Benjavalero\",\"revisions\":[{\"timestamp\": \"2016-02-26T21:48:59Z\",\"slots\":{\"main\":{\"contentmodel\":\"wikitext\",\"contentformat\":\"text/x-wiki\",\"content\":\"Soy de [[Orihuela]]\"}}}]},{\"pageid\":6903884,\"ns\":2,\"title\":\"Usuario:Benjavalero/Taller\",\"revisions\":[{\"timestamp\": \"2016-02-26T21:48:59Z\",\"slots\":{\"main\":{\"contentmodel\":\"wikitext\",\"contentformat\":\"text/x-wiki\",\"content\":\"Enlace a [[Pais Vasco]].\"}}}]}]}}";
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn(textResponse);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        List<WikipediaPage> pages = wikipediaService.getPagesByIds(Arrays.asList(6219990, 6903884));
        Assert.assertNotNull(pages);
        Assert.assertEquals(2, pages.size());
        Assert.assertTrue(pages.stream().anyMatch(page -> page.getId() == 6219990));
        Assert.assertTrue(pages.stream().filter(page -> page.getId() == 6219990).findAny().orElseThrow(WikipediaException::new).getContent().contains("Orihuela"));
        Assert.assertTrue(pages.stream().anyMatch(page -> page.getId() == 6903884));
        Assert.assertTrue(pages.stream().filter(page -> page.getId() == 6903884).findAny().orElseThrow(WikipediaException::new).getContent().contains("Pais Vasco"));
    }

    @Test(expected = WikipediaException.class)
    public void testGetPagesContentWithErrors() throws WikipediaException, InterruptedException, ExecutionException, IOException {
        // API response
        String textResponse = "{\"error\":{\"code\":\"too-many-pageids\",\"info\":\"Too many values supplied for parameter \\\"pageids\\\". The limit is 50.\",\"docref\":\"See https://es.wikipedia.org/w/api.php for API usage. Subscribe to the mediawiki-api-announce mailing list at &lt;https://lists.wikimedia.org/mailman/listinfo/mediawiki-api-announce&gt; for notice of API deprecations and breaking changes.\"},\"servedby\":\"mw1342\"}";
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn(textResponse);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        wikipediaService.getPagesByIds(Collections.singletonList(6219990));
    }

    @Test
    public void testGetPageContentUnavailable() throws WikipediaException, InterruptedException, ExecutionException, IOException {
        // API response
        String textResponse = "{\"batchcomplete\":true,\"curtimestamp\": \"2019-06-13T10:41:02Z\",\"query\":{\"pages\":[{\"ns\":2,\"title\":\"Usuario:Benjavaleroxx\",\"missing\":true}]}}";
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn(textResponse);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        Assert.assertFalse(wikipediaService.getPageByTitle("Usuario:Benjavaleroxx").isPresent());
    }

    @Test
    public void testGetPageIdsByStringMatch() throws WikipediaException, InterruptedException, ExecutionException, IOException {
        // API response
        String textResponse = "{\"batchcomplete\":\"\",\"continue\":{\"sroffset\":10,\"continue\":\"-||\"},\"query\":{\"search\":[{\"ns\":0,\"title\":\"Belanova\",\"pageid\":297896},{\"ns\":0,\"title\":\"Wil Hartog\",\"pageid\":7694956},{\"ns\":0,\"title\":\"Compuesto químico\",\"pageid\":10547},{\"ns\":0,\"title\":\"Aun así te vas\",\"pageid\":2460037},{\"ns\":0,\"title\":\"Educación\",\"pageid\":975},{\"ns\":0,\"title\":\"Abolicionismo\",\"pageid\":173068},{\"ns\":0,\"title\":\"Canaán\",\"pageid\":718871},{\"ns\":0,\"title\":\"Coahuila de Zaragoza\",\"pageid\":724588},{\"ns\":0,\"title\":\"Filosofía\",\"pageid\":689592},{\"ns\":0,\"title\":\"Cárites\",\"pageid\":71433}]}}";
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn(textResponse);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        Set<Integer> pageIds = wikipediaService.getPageIdsByStringMatch("");
        Assert.assertEquals(10, pageIds.size());
    }

    @Test
    public void testGetPageIdsByStringMatchWithNoResults() throws WikipediaException, InterruptedException, ExecutionException, IOException {
        // API response
        String textResponse = "{\"batchcomplete\":\"\",\"query\":{\"search\":[]}}";
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn(textResponse);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        Set<Integer> pageIds = wikipediaService.getPageIdsByStringMatch("");
        Assert.assertTrue(pageIds.isEmpty());
    }

    @Test
    public void testIdentify() throws WikipediaException, InterruptedException, ExecutionException, IOException {
        // API response
        String textResponse = "{\"batchcomplete\": \"\", \"query\": {\"userinfo\": {\"id\": 9620478, \"name\": \"Benjavalero\"}}}";
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn(textResponse);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        OAuth1AccessToken accessToken = new OAuth1AccessToken("", "");
        String username = wikipediaService.identify(accessToken);
        Assert.assertEquals("Benjavalero", username);
    }

    @Test(expected = WikipediaException.class)
    public void testSavePageContentWithConflict() throws WikipediaException, InterruptedException, ExecutionException, IOException {
        // API response for the EditToken request
        String textResponse = "{\"batchcomplete\":true,\"query\":{\"pages\":[{\"pageid\":2209245,\"ns\":4,\"title\":\"Wikipedia:Zona de pruebas/5\",\"revisions\":[{\"timestamp\":\"2019-06-24T21:24:09Z\"}]}],\"tokens\":{\"csrftoken\":\"+\\\\\"}}}";
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn(textResponse);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        OAuth1AccessToken accessToken = new OAuth1AccessToken("", "");
        String timestamp = WikipediaPage.formatWikipediaTimestamp(LocalDateTime.now().withYear(2018));
        wikipediaService.savePageContent(1, "", 0, timestamp, accessToken);

        Mockito.verify(oAuthService, Mockito.times(0)).execute(Mockito.any(OAuthRequest.class));
    }

    @Test
    public void testSavePageContent() throws WikipediaException, InterruptedException, ExecutionException, IOException {
        // API response for the EditToken request
        String textResponse = "{\"batchcomplete\":true,\"query\":{\"pages\":[{\"pageid\":2209245,\"ns\":4,\"title\":\"Wikipedia:Zona de pruebas/5\",\"revisions\":[{\"timestamp\":\"2019-06-24T21:24:09Z\"}]}],\"tokens\":{\"csrftoken\":\"+\\\\\"}}}";
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn(textResponse);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        OAuth1AccessToken accessToken = new OAuth1AccessToken("", "");
        String timestamp = WikipediaPage.formatWikipediaTimestamp(LocalDateTime.now());
        wikipediaService.savePageContent(1, "", 0, timestamp, accessToken);

        // Two calls: one for the EditToken and another to save the content
        Mockito.verify(oAuthService, Mockito.times(2)).execute(Mockito.any(OAuthRequest.class));
    }

    @Test
    public void testBuildSearchExpressionCaseSensitive() {
        String text = "en Abril";
        String expected = "\"en Abril\" insource:/\"en Abril\"/";
        Assert.assertEquals(expected, wikipediaService.buildSearchExpression(text));
    }

    @Test
    public void testBuildSearchExpressionCaseInsensitive() {
        String text = "en abril";
        String expected = "\"en abril\"";
        Assert.assertEquals(expected, wikipediaService.buildSearchExpression(text));
    }

    @Test
    public void testGetPageSections() throws WikipediaException, InterruptedException, ExecutionException, IOException {
        // API response
        String textResponse = "{\"parse\":{\"title\":\"Usuario:Benjavalero/Taller\",\"pageid\":6903884,\"sections\":[{\"toclevel\":1,\"level\":\"2\",\"line\":\"Pruebas con cursiva\",\"number\":\"1\",\"index\":\"1\",\"fromtitle\":\"Usuario:Benjavalero/Taller\",\"byteoffset\":1998,\"anchor\":\"Pruebas_con_cursiva\"},{\"toclevel\":1,\"level\":\"2\",\"line\":\"Pruebas de banderas de la Selección Española\",\"number\":\"2\",\"index\":\"2\",\"fromtitle\":\"Usuario:Benjavalero/Taller\",\"byteoffset\":2275,\"anchor\":\"Pruebas_de_banderas_de_la_Selección_Española\"},{\"toclevel\":1,\"level\":\"2\",\"line\":\"Referencias\",\"number\":\"3\",\"index\":\"3\",\"fromtitle\":\"Usuario:Benjavalero/Taller\",\"byteoffset\":2497,\"anchor\":\"Referencias\"}]}}";
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getBody()).thenReturn(textResponse);
        Mockito.when(response.isSuccessful()).thenReturn(true);
        Mockito.when(oAuthService.execute(Mockito.any(OAuthRequest.class))).thenReturn(response);

        List<WikipediaSection> sections = wikipediaService.getPageSections(6903884);
        Assert.assertNotNull(sections);
        Assert.assertEquals(3, sections.size());
        Assert.assertTrue(sections.stream().anyMatch(sec -> sec.getIndex() == 1));
        Assert.assertEquals("Pruebas con cursiva", sections.stream()
                .filter(sec -> sec.getNumber().equals("1"))
                .findAny().orElseThrow(WikipediaException::new).getLine());
        Assert.assertEquals(1998, sections.stream()
                .filter(sec -> sec.getNumber().equals("1"))
                .findAny().orElseThrow(WikipediaException::new).getByteOffset());

        Assert.assertTrue(sections.stream().anyMatch(sec -> sec.getIndex() == 2));
        Assert.assertEquals("Pruebas de banderas de la Selección Española", sections.stream()
                .filter(sec -> sec.getNumber().equals("2"))
                .findAny().orElseThrow(WikipediaException::new).getLine());
        Assert.assertEquals(2275, sections.stream()
                .filter(sec -> sec.getNumber().equals("2"))
                .findAny().orElseThrow(WikipediaException::new).getByteOffset());

        Assert.assertTrue(sections.stream().anyMatch(sec -> sec.getIndex() == 3));
        Assert.assertEquals("Referencias", sections.stream()
                .filter(sec -> sec.getNumber().equals("3"))
                .findAny().orElseThrow(WikipediaException::new).getLine());
        Assert.assertEquals(2497, sections.stream()
                .filter(sec -> sec.getNumber().equals("3"))
                .findAny().orElseThrow(WikipediaException::new).getByteOffset());
    }

}
