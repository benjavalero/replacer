package es.bvalero.replacer.wikipedia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class WikipediaServiceTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private WikipediaServiceImpl wikipediaService;

    private ObjectMapper jsonMapper;

    @Before
    public void setUp() {
        jsonMapper = new ObjectMapper();
        wikipediaService = new WikipediaServiceImpl();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetEditToken() throws IOException, AuthenticationException, WikipediaException {
        // API response
        String textResponse = "{\"batchcomplete\":true,\"query\":{\"tokens\":{\"csrftoken\":\"+\\\\\"}}}";
        JsonNode jsonResponse = jsonMapper.readTree(textResponse);
        Mockito.when(authenticationService.executeOAuthRequest(Mockito.anyMap(), Mockito.nullable(OAuth1AccessToken.class)))
                .thenReturn(jsonResponse);

        // We pass a null access token to retrieve an anonymous edit token
        String editToken = wikipediaService.getEditToken(null);
        Assert.assertNotNull(editToken);
        Assert.assertTrue(editToken.endsWith("+\\"));
    }

    @Test
    public void testGetPageContent() throws IOException, AuthenticationException, WikipediaException {
        // API response
        String textResponse = "{\"batchcomplete\":true,\"query\":{\"pages\":[{\"pageid\":6219990,\"ns\":2,\"title\":\"Usuario:Benjavalero\",\"revisions\":[{\"slots\":{\"main\":{\"contentmodel\":\"wikitext\",\"contentformat\":\"text/x-wiki\",\"content\":\"Soy de [[Orihuela]]\"}}}]}]}}";
        JsonNode jsonResponse = jsonMapper.readTree(textResponse);
        Mockito.when(authenticationService.executeOAuthRequest(Mockito.anyMap(), Mockito.nullable(OAuth1AccessToken.class)))
                .thenReturn(jsonResponse);

        String pageContent = wikipediaService.getPageContent("Usuario:Benjavalero");
        Assert.assertNotNull(pageContent);
        Assert.assertTrue(pageContent.contains("Orihuela"));
    }

    @Test
    public void testGetPagesContent() throws IOException, AuthenticationException, WikipediaException {
        // API response
        String textResponse = "{\"batchcomplete\":true,\"query\":{\"pages\":[{\"pageid\":6219990,\"ns\":2,\"title\":\"Usuario:Benjavalero\",\"revisions\":[{\"slots\":{\"main\":{\"contentmodel\":\"wikitext\",\"contentformat\":\"text/x-wiki\",\"content\":\"Soy de [[Orihuela]]\"}}}]},{\"pageid\":6903884,\"ns\":2,\"title\":\"Usuario:Benjavalero/Taller\",\"revisions\":[{\"slots\":{\"main\":{\"contentmodel\":\"wikitext\",\"contentformat\":\"text/x-wiki\",\"content\":\"Enlace a [[Pais Vasco]].\"}}}]}]}}";
        JsonNode jsonResponse = jsonMapper.readTree(textResponse);
        Mockito.when(authenticationService.executeOAuthRequest(Mockito.anyMap(), Mockito.nullable(OAuth1AccessToken.class)))
                .thenReturn(jsonResponse);

        Map<Integer, String> pagesContent = wikipediaService.getPagesContent(Arrays.asList(6219990, 6903884), null);
        Assert.assertNotNull(pagesContent);
        Assert.assertEquals(2, pagesContent.size());
        Assert.assertTrue(pagesContent.containsKey(6219990));
        Assert.assertTrue(pagesContent.get(6219990).contains("Orihuela"));
        Assert.assertTrue(pagesContent.containsKey(6903884));
        Assert.assertTrue(pagesContent.get(6903884).contains("Pais Vasco"));
    }

    @Test(expected = WikipediaException.class)
    public void testGetPagesContentWithErrors() throws IOException, AuthenticationException, WikipediaException {
        // API response
        String textResponse = "{\"error\":{\"code\":\"too-many-pageids\",\"info\":\"Too many values supplied for parameter \\\"pageids\\\". The limit is 50.\",\"docref\":\"See https://es.wikipedia.org/w/api.php for API usage. Subscribe to the mediawiki-api-announce mailing list at &lt;https://lists.wikimedia.org/mailman/listinfo/mediawiki-api-announce&gt; for notice of API deprecations and breaking changes.\"},\"servedby\":\"mw1342\"}";
        JsonNode jsonResponse = jsonMapper.readTree(textResponse);
        Mockito.when(authenticationService.executeOAuthRequest(Mockito.anyMap(), Mockito.nullable(OAuth1AccessToken.class)))
                .thenReturn(jsonResponse);

        wikipediaService.getPagesContent(Collections.singletonList(6219990), null);
    }

    @Test(expected = UnavailablePageException.class)
    public void testGetPageContentUnavailable() throws IOException, AuthenticationException, WikipediaException {
        // API response
        String textResponse = "{\"batchcomplete\":true,\"query\":{\"pages\":[{\"ns\":2,\"title\":\"Usuario:Benjavaleroxx\",\"missing\":true}]}}";
        JsonNode jsonResponse = jsonMapper.readTree(textResponse);
        Mockito.when(authenticationService.executeOAuthRequest(Mockito.anyMap(), Mockito.nullable(OAuth1AccessToken.class)))
                .thenReturn(jsonResponse);

        wikipediaService.getPageContent("Usuario:Benjavaleroxx");
    }

}
