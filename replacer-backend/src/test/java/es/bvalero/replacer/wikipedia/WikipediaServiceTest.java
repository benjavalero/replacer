package es.bvalero.replacer.wikipedia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.ReplacerException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;

public class WikipediaServiceTest {

    @Spy
    private ObjectMapper jsonMapper;

    @Spy
    private ModelMapper modelMapper;

    @Mock
    private WikipediaRequestService wikipediaRequestService;

    @InjectMocks
    private WikipediaServiceImpl wikipediaService;

    private WikipediaService wikipediaServiceOffline;

    @Before
    public void setUp() {
        wikipediaService = new WikipediaServiceImpl();
        wikipediaServiceOffline = new WikipediaServiceOfflineImpl();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetEditToken() throws Exception {
        // API response
        String textResponse = "{\"batchcomplete\":true,\"query\":{\"pages\":[{\"pageid\":2209245,\"ns\":4,\"title\":\"Wikipedia:Zona de pruebas/5\",\"revisions\":[{\"timestamp\":\"2019-06-24T21:24:09Z\"}]}],\"tokens\":{\"csrftoken\":\"+\\\\\"}}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        Mockito.when(wikipediaRequestService.executeSignedPostRequest(Mockito.anyMap(), Mockito.any(WikipediaLanguage.class), Mockito.any(OAuth1AccessToken.class)))
                .thenReturn(response);
        Assert.assertTrue(response.isBatchcomplete());

        // We pass a null access token to retrieve an anonymous edit token
        EditToken editToken = wikipediaService.getEditToken(2209245, WikipediaLanguage.SPANISH, new OAuth1AccessToken("", ""));
        Assert.assertNotNull(editToken.getCsrfToken());
        Assert.assertEquals("+\\", editToken.getCsrfToken());
        Assert.assertEquals("2019-06-24T21:24:09Z", editToken.getTimestamp());
    }

    @Test
    public void testGetPageContentByTitle() throws Exception {
        // API response
        String textResponse = "{\"batchcomplete\":true,\"curtimestamp\": \"2019-06-13T10:41:02Z\",\"query\":{\"pages\":[{\"pageid\":6219990,\"ns\":2,\"title\":\"Usuario:Benjavalero\",\"revisions\":[{\"timestamp\": \"2016-02-26T21:48:59Z\",\"slots\":{\"main\":{\"contentmodel\":\"wikitext\",\"contentformat\":\"text/x-wiki\",\"content\":\"Soy de [[Orihuela]]\"}}}]}]}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        Mockito.when(wikipediaRequestService.executeGetRequest(Mockito.anyMap(), Mockito.any(WikipediaLanguage.class))).thenReturn(response);

        int pageId = 6219990;
        String title = "Usuario:Benjavalero";
        WikipediaPage page = wikipediaService.getPageByTitle(title, WikipediaLanguage.SPANISH)
                .orElseThrow(ReplacerException::new);
        Assert.assertNotNull(page);
        Assert.assertEquals(pageId, page.getId());
        Assert.assertEquals(title, page.getTitle());
        Assert.assertEquals(WikipediaNamespace.USER, page.getNamespace());
        Assert.assertTrue(page.getLastUpdate().getYear() >= 2016);
        Assert.assertTrue(page.getContent().contains("Orihuela"));
    }

    @Test
    public void testGetPageContentById() throws Exception {
        // API response
        String textResponse = "{\"batchcomplete\":true,\"curtimestamp\": \"2019-06-13T10:41:02Z\",\"query\":{\"pages\":[{\"pageid\":6219990,\"ns\":2,\"title\":\"Usuario:Benjavalero\",\"revisions\":[{\"timestamp\": \"2016-02-26T21:48:59Z\",\"slots\":{\"main\":{\"contentmodel\":\"wikitext\",\"contentformat\":\"text/x-wiki\",\"content\":\"Soy de [[Orihuela]]\"}}}]}]}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        Mockito.when(wikipediaRequestService.executeGetRequest(Mockito.anyMap(), Mockito.any(WikipediaLanguage.class))).thenReturn(response);

        int pageId = 6219990;
        String title = "Usuario:Benjavalero";
        WikipediaPage page = wikipediaService.getPageById(pageId, WikipediaLanguage.SPANISH)
                .orElseThrow(ReplacerException::new);
        Assert.assertNotNull(page);
        Assert.assertEquals(pageId, page.getId());
        Assert.assertEquals(title, page.getTitle());
        Assert.assertEquals(WikipediaNamespace.USER, page.getNamespace());
        Assert.assertTrue(page.getLastUpdate().getYear() >= 2016);
        Assert.assertTrue(page.getContent().contains("Orihuela"));
    }

    @Test
    public void testGetPagesContent() throws Exception {
        // API response
        String textResponse = "{\"batchcomplete\":true,\"curtimestamp\": \"2019-06-13T10:41:02Z\",\"query\":{\"pages\":[{\"pageid\":6219990,\"ns\":2,\"title\":\"Usuario:Benjavalero\",\"revisions\":[{\"timestamp\": \"2016-02-26T21:48:59Z\",\"slots\":{\"main\":{\"contentmodel\":\"wikitext\",\"contentformat\":\"text/x-wiki\",\"content\":\"Soy de [[Orihuela]]\"}}}]},{\"pageid\":6903884,\"ns\":2,\"title\":\"Usuario:Benjavalero/Taller\",\"revisions\":[{\"timestamp\": \"2016-02-26T21:48:59Z\",\"slots\":{\"main\":{\"contentmodel\":\"wikitext\",\"contentformat\":\"text/x-wiki\",\"content\":\"Enlace a [[Pais Vasco]].\"}}}]}]}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        Mockito.when(wikipediaRequestService.executeGetRequest(Mockito.anyMap(), Mockito.any(WikipediaLanguage.class))).thenReturn(response);

        List<WikipediaPage> pages = wikipediaService.getPagesByIds(Arrays.asList(6219990, 6903884), WikipediaLanguage.SPANISH);
        Assert.assertNotNull(pages);
        Assert.assertEquals(2, pages.size());
        Assert.assertTrue(pages.stream().anyMatch(page -> page.getId() == 6219990));
        Assert.assertTrue(pages.stream().filter(page -> page.getId() == 6219990).findAny().orElseThrow(ReplacerException::new).getContent().contains("Orihuela"));
        Assert.assertTrue(pages.stream().anyMatch(page -> page.getId() == 6903884));
        Assert.assertTrue(pages.stream().filter(page -> page.getId() == 6903884).findAny().orElseThrow(ReplacerException::new).getContent().contains("Pais Vasco"));
    }

    @Test
    public void testGetPageContentUnavailable() throws Exception {
        // API response
        String textResponse = "{\"batchcomplete\":true,\"curtimestamp\": \"2019-06-13T10:41:02Z\",\"query\":{\"pages\":[{\"ns\":2,\"title\":\"Usuario:Benjavaleroxx\",\"missing\":true}]}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        Mockito.when(wikipediaRequestService.executeGetRequest(Mockito.anyMap(), Mockito.any(WikipediaLanguage.class))).thenReturn(response);

        Assert.assertFalse(wikipediaService.getPageByTitle("Usuario:Benjavaleroxx", WikipediaLanguage.SPANISH).isPresent());
    }

    @Test
    public void testGetPageIdsByStringMatch() throws Exception {
        // API response
        String textResponse = "{\"batchcomplete\":\"\",\"continue\":{\"sroffset\":100,\"continue\":\"-||\"},\"query\":{\"searchinfo\":{\"totalhits\":10},\"search\":[{\"ns\":0,\"title\":\"Belanova\",\"pageid\":297896},{\"ns\":0,\"title\":\"Wil Hartog\",\"pageid\":7694956},{\"ns\":0,\"title\":\"Compuesto químico\",\"pageid\":10547},{\"ns\":0,\"title\":\"Aun así te vas\",\"pageid\":2460037},{\"ns\":0,\"title\":\"Educación\",\"pageid\":975},{\"ns\":0,\"title\":\"Abolicionismo\",\"pageid\":173068},{\"ns\":0,\"title\":\"Canaán\",\"pageid\":718871},{\"ns\":0,\"title\":\"Coahuila de Zaragoza\",\"pageid\":724588},{\"ns\":0,\"title\":\"Filosofía\",\"pageid\":689592},{\"ns\":0,\"title\":\"Cárites\",\"pageid\":71433}]}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        Mockito.when(wikipediaRequestService.executeGetRequest(Mockito.anyMap(), Mockito.any(WikipediaLanguage.class))).thenReturn(response);

        PageSearchResult pageIds = wikipediaService.getPageIdsByStringMatch("", 0, 100, WikipediaLanguage.SPANISH);
        Assert.assertEquals(10, pageIds.getTotal());
    }

    @Test
    public void testGetPageIdsByStringMatchWithNoResults() throws Exception {
        // API response
        String textResponse = "{\"batchcomplete\":\"\",\"query\":{\"searchinfo\":{\"totalhits\":0},\"search\":[]}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        Mockito.when(wikipediaRequestService.executeGetRequest(Mockito.anyMap(), Mockito.any(WikipediaLanguage.class))).thenReturn(response);

        PageSearchResult pageIds = wikipediaService.getPageIdsByStringMatch("", 0, 100, WikipediaLanguage.SPANISH);
        Assert.assertTrue(pageIds.isEmpty());
    }

    @Test
    public void testLoggedUserName() throws Exception {
        // API response
        String textResponse = "{\"batchcomplete\": \"\", \"query\": {\"userinfo\": {\"id\": 9620478, \"name\": \"Benjavalero\"}}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        Mockito.when(wikipediaRequestService.executeSignedGetRequest(Mockito.anyMap(), Mockito.any(WikipediaLanguage.class), Mockito.any(OAuth1AccessToken.class)))
                .thenReturn(response);

        OAuth1AccessToken accessToken = new OAuth1AccessToken("", "");
        String username = wikipediaService.getLoggedUserName(accessToken, WikipediaLanguage.SPANISH);
        Assert.assertEquals("Benjavalero", username);
    }

    @Test(expected = ReplacerException.class)
    public void testSavePageContentWithConflict() throws Exception {
        // API response for the EditToken request
        String textResponse = "{\"batchcomplete\":true,\"query\":{\"pages\":[{\"pageid\":2209245,\"ns\":4,\"title\":\"Wikipedia:Zona de pruebas/5\",\"revisions\":[{\"timestamp\":\"2019-06-24T21:24:09Z\"}]}],\"tokens\":{\"csrftoken\":\"+\\\\\"}}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        Mockito.when(wikipediaRequestService.executeSignedPostRequest(Mockito.anyMap(), Mockito.any(WikipediaLanguage.class), Mockito.any(OAuth1AccessToken.class)))
                .thenReturn(response);

        OAuth1AccessToken accessToken = new OAuth1AccessToken("", "");
        // We use a timestamp BEFORE the timestamp of the last edition (from the edit token)
        String currentTimestamp = "2019-06-23T21:24:09Z";
        wikipediaService.savePageContent(1, "", 0, currentTimestamp, WikipediaLanguage.SPANISH, accessToken);

        Mockito.verify(wikipediaRequestService, Mockito.times(0))
                .executeSignedPostRequest(Mockito.anyMap(), Mockito.any(WikipediaLanguage.class), Mockito.any(OAuth1AccessToken.class));
    }

    @Test
    public void testSavePageContent() throws Exception {
        // API response for the EditToken request
        String textResponse = "{\"batchcomplete\":true,\"query\":{\"pages\":[{\"pageid\":2209245,\"ns\":4,\"title\":\"Wikipedia:Zona de pruebas/5\",\"revisions\":[{\"timestamp\":\"2019-06-24T21:24:09Z\"}]}],\"tokens\":{\"csrftoken\":\"+\\\\\"}}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        Mockito.when(wikipediaRequestService.executeSignedPostRequest(Mockito.anyMap(), Mockito.any(WikipediaLanguage.class), Mockito.any(OAuth1AccessToken.class)))
                .thenReturn(response);

        OAuth1AccessToken accessToken = new OAuth1AccessToken("", "");
        // We use a timestamp AFTER the timestamp of the last edition (from the edit token)
        String currentTimestamp = "2019-06-25T21:24:09Z";
        wikipediaService.savePageContent(1, "", null, currentTimestamp, WikipediaLanguage.SPANISH, accessToken);

        // Two calls: one for the EditToken and another to save the content
        Mockito.verify(wikipediaRequestService, Mockito.times(2))
                .executeSignedPostRequest(Mockito.anyMap(), Mockito.any(WikipediaLanguage.class), Mockito.any(OAuth1AccessToken.class));

        // Save a section
        // We use a timestamp AFTER the timestamp of the last edition (from the edit token)
        currentTimestamp = "2019-06-26T21:24:09Z";
        wikipediaService.savePageContent(1, "", 2, currentTimestamp, WikipediaLanguage.SPANISH, accessToken);

        // Two calls: one for the EditToken and another to save the content (x2 save page and section in this test)
        Mockito.verify(wikipediaRequestService, Mockito.times(4))
                .executeSignedPostRequest(Mockito.anyMap(), Mockito.any(WikipediaLanguage.class), Mockito.any(OAuth1AccessToken.class));
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
    public void testGetPageSections() throws Exception {
        // API response
        String textResponse = "{\"parse\":{\"title\":\"Usuario:Benjavalero/Taller\",\"pageid\":6903884,\"sections\":[{\"toclevel\":1,\"level\":\"2\",\"line\":\"Pruebas con cursiva\",\"number\":\"1\",\"index\":\"1\",\"fromtitle\":\"Usuario:Benjavalero/Taller\",\"byteoffset\":1998,\"anchor\":\"Pruebas_con_cursiva\"},{\"toclevel\":1,\"level\":\"2\",\"line\":\"Pruebas de banderas de la Selección Española\",\"number\":\"2\",\"index\":\"2\",\"fromtitle\":\"Usuario:Benjavalero/Taller\",\"byteoffset\":2275,\"anchor\":\"Pruebas_de_banderas_de_la_Selección_Española\"},{\"toclevel\":1,\"level\":\"2\",\"line\":\"Referencias\",\"number\":\"3\",\"index\":\"3\",\"fromtitle\":\"Usuario:Benjavalero/Taller\",\"byteoffset\":2497,\"anchor\":\"Referencias\"}]}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        Mockito.when(wikipediaRequestService.executeGetRequest(Mockito.anyMap(), Mockito.any(WikipediaLanguage.class))).thenReturn(response);

        List<WikipediaSection> sections = wikipediaService.getPageSections(6903884, WikipediaLanguage.SPANISH);
        Assert.assertNotNull(sections);
        Assert.assertEquals(3, sections.size());

        Assert.assertTrue(sections.stream().anyMatch(sec -> sec.getIndex() == 1));
        Assert.assertEquals(1998, sections.stream()
                .filter(sec -> sec.getIndex() == 1)
                .findAny().orElseThrow(ReplacerException::new).getByteOffset());

        Assert.assertTrue(sections.stream().anyMatch(sec -> sec.getIndex() == 2));
        Assert.assertEquals(2275, sections.stream()
                .filter(sec -> sec.getIndex() == 2)
                .findAny().orElseThrow(ReplacerException::new).getByteOffset());

        Assert.assertTrue(sections.stream().anyMatch(sec -> sec.getIndex() == 3));
        Assert.assertEquals(2497, sections.stream()
                .filter(sec -> sec.getIndex() == 3)
                .findAny().orElseThrow(ReplacerException::new).getByteOffset());

        Assert.assertTrue(sections.stream().allMatch(sec -> StringUtils.isNotEmpty(sec.getAnchor())));
    }

    @Test
    public void testGetPageInvalidSections() throws Exception {
        // API response
        String textResponse = "{\"parse\":{\"title\":\"Anexo:Asteroides (161001)\\u2013(162000)\",\"pageid\":6633556,\"sections\":[{\"toclevel\":1,\"level\":\"2\",\"line\":\"Asteroides del (161001) al (161100)\",\"number\":\"1\",\"index\":\"\",\"byteoffset\":null,\"anchor\":\"Asteroides_del_(161001)_al_(161100)\"},{\"toclevel\":1,\"level\":\"2\",\"line\":\"Asteroides del (161101) al (161200)\",\"number\":\"2\",\"index\":\"\",\"byteoffset\":null,\"anchor\":\"Asteroides_del_(161101)_al_(161200)\"}]}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        Mockito.when(wikipediaRequestService.executeGetRequest(Mockito.anyMap(), Mockito.any(WikipediaLanguage.class))).thenReturn(response);

        List<WikipediaSection> sections = wikipediaService.getPageSections(6633556, WikipediaLanguage.SPANISH);
        Assert.assertNotNull(sections);
        Assert.assertTrue(sections.isEmpty());
    }

    @Test
    public void testIsAdminUser() {
        wikipediaService.setAdminUser("X");
        Assert.assertTrue(wikipediaService.isAdminUser("X"));
        Assert.assertFalse(wikipediaService.isAdminUser("Y"));
    }

    @Test
    public void testGetPageContentByIdAndSection() throws Exception {
        // API response
        String textResponse = "{\"batchcomplete\":true,\"curtimestamp\":\"2019-10-17T15:12:03Z\",\"query\":{\"pages\":[{\"pageid\":6903884,\"ns\":2,\"title\":\"Usuario:Benjavalero/Taller\",\"revisions\":[{\"timestamp\":\"2019-08-24T07:51:05Z\",\"slots\":{\"main\":{\"contentmodel\":\"wikitext\",\"contentformat\":\"text/x-wiki\",\"content\":\"== Pruebas con cursiva ==\\n\\n* El libro ''La historia interminable''.\\n* Comillas sin cerrar: ''La historia interminable\\n* Con negrita ''La '''historia''' interminable''.\\n* Con cursiva ''La ''historia'' interminable''.\\n* Con negrita buena ''La '''''historia''''' interminable''.\\n\\n=== Pruebas de subsecciones ===\\n\\nEsta es una subsección tonta solo para probar la captura de secciones.\"}}}]}]}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        Mockito.when(wikipediaRequestService.executeGetRequest(Mockito.anyMap(), Mockito.any(WikipediaLanguage.class))).thenReturn(response);

        int pageId = 6903884;
        int sectionId = 1;
        WikipediaSection section = new WikipediaSection();
        section.setIndex(sectionId);
        String title = "Usuario:Benjavalero/Taller";
        WikipediaPage page = wikipediaService.getPageByIdAndSection(pageId, section, WikipediaLanguage.SPANISH)
                .orElseThrow(ReplacerException::new);
        Assert.assertNotNull(page);
        Assert.assertEquals(pageId, page.getId());
        Assert.assertEquals(title, page.getTitle());
        Assert.assertEquals(WikipediaNamespace.USER, page.getNamespace());
        Assert.assertTrue(page.getLastUpdate().getYear() >= 2019);
        Assert.assertTrue(page.getContent().startsWith("=="));
        Assert.assertEquals(Integer.valueOf(sectionId), page.getSection());
    }

    @Test
    public void testWikipediaServiceOffline() throws ReplacerException {
        Assert.assertEquals("offline", wikipediaServiceOffline.getLoggedUserName(Mockito.mock(OAuth1AccessToken.class), WikipediaLanguage.SPANISH));
        Assert.assertTrue(wikipediaServiceOffline.isAdminUser(""));
        Assert.assertTrue(StringUtils.isNotBlank(wikipediaServiceOffline.getMisspellingListPageContent(WikipediaLanguage.SPANISH)));
        Assert.assertTrue(StringUtils.isNotBlank(wikipediaServiceOffline.getComposedMisspellingListPageContent(WikipediaLanguage.SPANISH)));
        Assert.assertTrue(StringUtils.isNotBlank(wikipediaServiceOffline.getFalsePositiveListPageContent(WikipediaLanguage.SPANISH)));
        Assert.assertEquals(Integer.valueOf(1), wikipediaServiceOffline.getPageByTitle("", WikipediaLanguage.SPANISH).map(WikipediaPage::getId).orElse(0));
        Assert.assertFalse(wikipediaServiceOffline.getPageById(1, WikipediaLanguage.SPANISH).map(WikipediaPage::getSection).isPresent());
        Assert.assertFalse(wikipediaServiceOffline.getPageIdsByStringMatch("", 0, 100, WikipediaLanguage.SPANISH).isEmpty());
        Assert.assertTrue(wikipediaServiceOffline.getPageSections(1, WikipediaLanguage.SPANISH).isEmpty());
        Assert.assertEquals(2, wikipediaServiceOffline.getPagesByIds(Arrays.asList(1, 2), WikipediaLanguage.SPANISH).size());
    }

}
