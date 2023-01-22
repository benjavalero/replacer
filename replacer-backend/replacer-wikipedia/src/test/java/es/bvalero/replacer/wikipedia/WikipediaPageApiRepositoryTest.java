package es.bvalero.replacer.wikipedia;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.user.AccessToken;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequest;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequestHelper;
import es.bvalero.replacer.wikipedia.api.WikipediaApiResponse;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

class WikipediaPageApiRepositoryTest {

    @Spy
    private ObjectMapper jsonMapper;

    @Mock
    private WikipediaApiRequestHelper wikipediaApiRequestHelper;

    @InjectMocks
    private WikipediaPageApiRepository wikipediaPageRepository;

    private WikipediaPageRepository wikipediaPageOfflineRepository;

    @BeforeEach
    void setUp() {
        wikipediaPageRepository = new WikipediaPageApiRepository();
        wikipediaPageOfflineRepository = new WikipediaPageOfflineRepository();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetEditToken() throws Exception {
        // API response
        String textResponse =
            "{\n" +
            "    \"batchcomplete\": true,\n" +
            "    \"query\": {\n" +
            "        \"pages\": [\n" +
            "            {\n" +
            "                \"pageid\": 2209245,\n" +
            "                \"ns\": 4,\n" +
            "                \"title\": \"Wikipedia:Zona de pruebas/5\",\n" +
            "                \"revisions\": [\n" +
            "                    {\n" +
            "                        \"timestamp\": \"2023-01-18T20:40:07Z\"\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        ],\n" +
            "        \"tokens\": {\n" +
            "            \"csrftoken\": \"332bb9c55bef953f93cb8391f8e6ee9e63c8fe90+\\\\\"\n" +
            "        }\n" +
            "    }\n" +
            "}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiRequestHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);
        assertTrue(response.isBatchcomplete());

        // We pass an empty access token to retrieve an anonymous edit token
        PageKey pageKey = PageKey.of(WikipediaLanguage.SPANISH, 2209245);
        EditToken editToken = wikipediaPageRepository.getEditToken(pageKey, AccessToken.empty());
        assertNotNull(editToken.getCsrfToken());
        assertEquals("332bb9c55bef953f93cb8391f8e6ee9e63c8fe90+\\", editToken.getCsrfToken());
        assertEquals("2023-01-18T20:40:07Z", editToken.getTimestamp().toString());
    }

    @Test
    void testGetPageContentByTitle() throws Exception {
        // API response
        String textResponse =
            "{\n" +
            "    \"batchcomplete\": true,\n" +
            "    \"curtimestamp\": \"2023-01-19T08:30:11Z\",\n" +
            "    \"query\": {\n" +
            "        \"pages\": [\n" +
            "            {\n" +
            "                \"pageid\": 6219990,\n" +
            "                \"ns\": 2,\n" +
            "                \"title\": \"Usuario:Benjavalero\",\n" +
            "                \"contentmodel\": \"wikitext\",\n" +
            "                \"pagelanguage\": \"es\",\n" +
            "                \"pagelanguagehtmlcode\": \"es\",\n" +
            "                \"pagelanguagedir\": \"ltr\",\n" +
            "                \"touched\": \"2023-01-18T11:59:55Z\",\n" +
            "                \"lastrevid\": 135919358,\n" +
            "                \"length\": 971,\n" +
            "                \"revisions\": [\n" +
            "                    {\n" +
            "                        \"timestamp\": \"2021-05-29T07:25:16Z\",\n" +
            "                        \"slots\": {\n" +
            "                            \"main\": {\n" +
            "                                \"contentmodel\": \"wikitext\",\n" +
            "                                \"contentformat\": \"text/x-wiki\",\n" +
            "                                \"content\": \"Soy de [[Orihuela]]\"\n" +
            "                            }\n" +
            "                        }\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiRequestHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        int pageId = 6219990;
        String title = "Usuario:Benjavalero";
        WikipediaPage page = wikipediaPageRepository
            .findByTitle(WikipediaLanguage.SPANISH, title)
            .orElseThrow(WikipediaException::new);
        assertNotNull(page);
        assertEquals(pageId, page.getPageId());
        assertEquals(title, page.getTitle());
        assertEquals(WikipediaNamespace.USER, page.getNamespace());
        assertTrue(page.getLastUpdate().toLocalDate().getYear() >= 2016);
        assertTrue(page.getContent().contains("Orihuela"));
    }

    @Test
    void testGetPageContentById() throws Exception {
        // API response
        String textResponse =
            "{\n" +
            "    \"batchcomplete\": true,\n" +
            "    \"curtimestamp\": \"2023-01-19T08:35:51Z\",\n" +
            "    \"query\": {\n" +
            "        \"pages\": [\n" +
            "            {\n" +
            "                \"pageid\": 6219990,\n" +
            "                \"ns\": 2,\n" +
            "                \"title\": \"Usuario:Benjavalero\",\n" +
            "                \"contentmodel\": \"wikitext\",\n" +
            "                \"pagelanguage\": \"es\",\n" +
            "                \"pagelanguagehtmlcode\": \"es\",\n" +
            "                \"pagelanguagedir\": \"ltr\",\n" +
            "                \"touched\": \"2023-01-18T11:59:55Z\",\n" +
            "                \"lastrevid\": 135919358,\n" +
            "                \"length\": 971,\n" +
            "                \"revisions\": [\n" +
            "                    {\n" +
            "                        \"timestamp\": \"2021-05-29T07:25:16Z\",\n" +
            "                        \"slots\": {\n" +
            "                            \"main\": {\n" +
            "                                \"contentmodel\": \"wikitext\",\n" +
            "                                \"contentformat\": \"text/x-wiki\",\n" +
            "                                \"content\": \"Soy de [[Orihuela]]\"\n" +
            "                            }\n" +
            "                        }\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiRequestHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        PageKey pageKey = PageKey.of(WikipediaLanguage.SPANISH, 6219990);
        String title = "Usuario:Benjavalero";
        WikipediaPage page = wikipediaPageRepository.findByKey(pageKey).orElseThrow(WikipediaException::new);
        assertNotNull(page);
        assertEquals(pageKey, page.getPageKey());
        assertEquals(title, page.getTitle());
        assertEquals(WikipediaNamespace.USER, page.getNamespace());
        assertTrue(page.getLastUpdate().toLocalDate().getYear() >= 2016);
        assertTrue(page.getContent().contains("Orihuela"));
    }

    @Test
    void testGetPagesContent() throws Exception {
        // API response
        String textResponse =
            "{\n" +
            "    \"batchcomplete\": true,\n" +
            "    \"curtimestamp\": \"2023-01-19T08:37:13Z\",\n" +
            "    \"query\": {\n" +
            "        \"pages\": [\n" +
            "            {\n" +
            "                \"pageid\": 6219990,\n" +
            "                \"ns\": 2,\n" +
            "                \"title\": \"Usuario:Benjavalero\",\n" +
            "                \"contentmodel\": \"wikitext\",\n" +
            "                \"pagelanguage\": \"es\",\n" +
            "                \"pagelanguagehtmlcode\": \"es\",\n" +
            "                \"pagelanguagedir\": \"ltr\",\n" +
            "                \"touched\": \"2023-01-18T11:59:55Z\",\n" +
            "                \"lastrevid\": 135919358,\n" +
            "                \"length\": 971,\n" +
            "                \"revisions\": [\n" +
            "                    {\n" +
            "                        \"timestamp\": \"2021-05-29T07:25:16Z\",\n" +
            "                        \"slots\": {\n" +
            "                            \"main\": {\n" +
            "                                \"contentmodel\": \"wikitext\",\n" +
            "                                \"contentformat\": \"text/x-wiki\",\n" +
            "                                \"content\": \"Soy de [[Orihuela]]\"\n" +
            "                            }\n" +
            "                        }\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"pageid\": 6903884,\n" +
            "                \"ns\": 2,\n" +
            "                \"title\": \"Usuario:Benjavalero/Taller\",\n" +
            "                \"contentmodel\": \"wikitext\",\n" +
            "                \"pagelanguage\": \"es\",\n" +
            "                \"pagelanguagehtmlcode\": \"es\",\n" +
            "                \"pagelanguagedir\": \"ltr\",\n" +
            "                \"touched\": \"2022-12-29T13:00:35Z\",\n" +
            "                \"lastrevid\": 147628235,\n" +
            "                \"length\": 8248,\n" +
            "                \"revisions\": [\n" +
            "                    {\n" +
            "                        \"timestamp\": \"2022-11-29T15:45:09Z\",\n" +
            "                        \"slots\": {\n" +
            "                            \"main\": {\n" +
            "                                \"contentmodel\": \"wikitext\",\n" +
            "                                \"contentformat\": \"text/x-wiki\",\n" +
            "                                \"content\": \"Enlace a [[Pais Vasco]].\"\n" +
            "                            }\n" +
            "                        }\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiRequestHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        WikipediaLanguage lang = WikipediaLanguage.SPANISH;
        Collection<WikipediaPage> pages = wikipediaPageRepository.findByKeys(
            List.of(PageKey.of(lang, 6219990), PageKey.of(lang, 6903884))
        );
        assertNotNull(pages);
        assertEquals(2, pages.size());
        assertTrue(pages.stream().anyMatch(page -> page.getPageId() == 6219990));
        assertTrue(
            pages
                .stream()
                .filter(page -> page.getPageId() == 6219990)
                .findAny()
                .orElseThrow(WikipediaException::new)
                .getContent()
                .contains("Orihuela")
        );
        assertTrue(pages.stream().anyMatch(page -> page.getPageId() == 6903884));
        assertTrue(
            pages
                .stream()
                .filter(page -> page.getPageId() == 6903884)
                .findAny()
                .orElseThrow(WikipediaException::new)
                .getContent()
                .contains("Pais Vasco")
        );
    }

    @Test
    void testGetPageContentUnavailable() throws Exception {
        // API response
        String textResponse =
            "{\n" +
            "    \"batchcomplete\": true,\n" +
            "    \"curtimestamp\": \"2023-01-19T08:38:51Z\",\n" +
            "    \"query\": {\n" +
            "        \"pages\": [\n" +
            "            {\n" +
            "                \"ns\": 2,\n" +
            "                \"title\": \"Usuario:Benjavaleroxx\",\n" +
            "                \"missing\": true,\n" +
            "                \"contentmodel\": \"wikitext\",\n" +
            "                \"pagelanguage\": \"es\",\n" +
            "                \"pagelanguagehtmlcode\": \"es\",\n" +
            "                \"pagelanguagedir\": \"ltr\"\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiRequestHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        assertFalse(
            wikipediaPageRepository.findByTitle(WikipediaLanguage.SPANISH, "Usuario:Benjavaleroxx").isPresent()
        );
    }

    @Test
    void testGetProtectedPage() throws Exception {
        // API response
        String textResponse =
            "{\n" +
            "    \"batchcomplete\": true,\n" +
            "    \"curtimestamp\": \"2023-01-19T09:07:39Z\",\n" +
            "    \"query\": {\n" +
            "        \"pages\": [\n" +
            "            {\n" +
            "                \"pageid\": 252086,\n" +
            "                \"ns\": 0,\n" +
            "                \"title\": \"Getxo\",\n" +
            "                \"contentmodel\": \"wikitext\",\n" +
            "                \"pagelanguage\": \"es\",\n" +
            "                \"pagelanguagehtmlcode\": \"es\",\n" +
            "                \"pagelanguagedir\": \"ltr\",\n" +
            "                \"touched\": \"2023-01-10T23:32:55Z\",\n" +
            "                \"lastrevid\": 8726615,\n" +
            "                \"length\": 20,\n" +
            "                \"redirect\": true,\n" +
            "                \"protection\": [\n" +
            "                    {\n" +
            "                        \"type\": \"edit\",\n" +
            "                        \"level\": \"sysop\",\n" +
            "                        \"expiry\": \"infinity\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"type\": \"move\",\n" +
            "                        \"level\": \"sysop\",\n" +
            "                        \"expiry\": \"infinity\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"restrictiontypes\": [\n" +
            "                    \"edit\",\n" +
            "                    \"move\"\n" +
            "                ],\n" +
            "                \"revisions\": [\n" +
            "                    {\n" +
            "                        \"timestamp\": \"2007-05-12T10:06:05Z\",\n" +
            "                        \"slots\": {\n" +
            "                            \"main\": {\n" +
            "                                \"contentmodel\": \"wikitext\",\n" +
            "                                \"contentformat\": \"text/x-wiki\",\n" +
            "                                \"content\": \"#REDIRECT [[Guecho]]\"\n" +
            "                            }\n" +
            "                        }\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiRequestHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        assertFalse(wikipediaPageRepository.findByTitle(WikipediaLanguage.SPANISH, "Getxo").isPresent());
    }

    @Test
    void testGetPageIdsByStringMatch() throws Exception {
        // API response
        String textResponse =
            "{\n" +
            "    \"batchcomplete\": true,\n" +
            "    \"query\": {\n" +
            "        \"searchinfo\": {\n" +
            "            \"totalhits\": 13\n" +
            "        },\n" +
            "        \"search\": [\n" +
            "            {\n" +
            "                \"ns\": 104,\n" +
            "                \"title\": \"Anexo:Municipios y comarcas de las Islas Baleares\",\n" +
            "                \"pageid\": 29088\n" +
            "            },\n" +
            "            {\n" +
            "                \"ns\": 104,\n" +
            "                \"title\": \"Anexo:Red de Carreteras de la Comunidad Valenciana\",\n" +
            "                \"pageid\": 637929\n" +
            "            },\n" +
            "            {\n" +
            "                \"ns\": 104,\n" +
            "                \"title\": \"Anexo:Armorial municipal de las Islas Baleares\",\n" +
            "                \"pageid\": 1418604\n" +
            "            },\n" +
            "            {\n" +
            "                \"ns\": 104,\n" +
            "                \"title\": \"Anexo:Cobertura de las Demarcaciones Locales de TDT\",\n" +
            "                \"pageid\": 2365909\n" +
            "            },\n" +
            "            {\n" +
            "                \"ns\": 104,\n" +
            "                \"title\": \"Anexo:Partidos judiciales de las Islas Baleares\",\n" +
            "                \"pageid\": 4489346\n" +
            "            },\n" +
            "            {\n" +
            "                \"ns\": 104,\n" +
            "                \"title\": \"Anexo:Bienes de interés cultural de las Islas Baleares\",\n" +
            "                \"pageid\": 5390126\n" +
            "            },\n" +
            "            {\n" +
            "                \"ns\": 104,\n" +
            "                \"title\": \"Anexo:The Amazing Race 21\",\n" +
            "                \"pageid\": 5397284\n" +
            "            },\n" +
            "            {\n" +
            "                \"ns\": 104,\n" +
            "                \"title\": \"Anexo:Bienes de relevancia local de la Marina Baja\",\n" +
            "                \"pageid\": 8684602\n" +
            "            },\n" +
            "            {\n" +
            "                \"ns\": 104,\n" +
            "                \"title\": \"Anexo:Monumentos de la comarca de la Marina Baja\",\n" +
            "                \"pageid\": 8753312\n" +
            "            },\n" +
            "            {\n" +
            "                \"ns\": 104,\n" +
            "                \"title\": \"Anexo:Monumentos de la ciudad de Valencia (A-L)\",\n" +
            "                \"pageid\": 8754198\n" +
            "            },\n" +
            "            {\n" +
            "                \"ns\": 104,\n" +
            "                \"title\": \"Anexo:Monumentos de la ciudad de Valencia (M-Z)\",\n" +
            "                \"pageid\": 8755195\n" +
            "            },\n" +
            "            {\n" +
            "                \"ns\": 104,\n" +
            "                \"title\": \"Anexo:Monumentos del distrito de Poblados del Sur\",\n" +
            "                \"pageid\": 8757203\n" +
            "            },\n" +
            "            {\n" +
            "                \"ns\": 104,\n" +
            "                \"title\": \"Anexo:Velódromos de Mallorca\",\n" +
            "                \"pageid\": 9436640\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiRequestHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        WikipediaSearchResult pageIds = wikipediaPageRepository.findByContent(
            WikipediaSearchRequest
                .builder()
                .lang(WikipediaLanguage.SPANISH)
                .namespaces(List.of(WikipediaNamespace.ANNEX))
                .text("Campaneta")
                .offset(0)
                .limit(100)
                .build()
        );
        assertEquals(13, pageIds.getTotal());
    }

    @Test
    void testGetPageIdsByStringMatchWithNoResults() throws Exception {
        // API response
        String textResponse =
            "{\n" +
            "    \"batchcomplete\": true,\n" +
            "    \"query\": {\n" +
            "        \"searchinfo\": {\n" +
            "            \"totalhits\": 0\n" +
            "        },\n" +
            "        \"search\": []\n" +
            "    }\n" +
            "}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiRequestHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        WikipediaSearchResult pageIds = wikipediaPageRepository.findByContent(
            WikipediaSearchRequest
                .builder()
                .lang(WikipediaLanguage.SPANISH)
                .namespaces(List.of(WikipediaNamespace.getDefault()))
                .text("jsdfslkdfjhow")
                .offset(0)
                .limit(100)
                .build()
        );
        assertTrue(pageIds.isEmpty());
    }

    @Test
    void testSavePageContentWithConflict() throws Exception {
        // API response for the EditToken request
        String textResponse =
            "{\"batchcomplete\":true,\"query\":{\"pages\":[{\"pageid\":2209245,\"ns\":4,\"title\":\"Wikipedia:Zona de pruebas/5\",\"revisions\":[{\"timestamp\":\"2019-06-24T21:24:09Z\"}]}],\"tokens\":{\"csrftoken\":\"+\\\\\"}}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiRequestHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        // We use a timestamp BEFORE the timestamp of the last edition (from the edit token)
        WikipediaTimestamp currentTimestamp = WikipediaTimestamp.of("2019-06-23T21:24:09Z");

        WikipediaPageSave pageSave = WikipediaPageSave
            .builder()
            .pageKey(PageKey.of(WikipediaLanguage.SPANISH, 1))
            .content("")
            .editSummary("")
            .queryTimestamp(currentTimestamp)
            .build();

        assertThrows(WikipediaException.class, () -> wikipediaPageRepository.save(pageSave, AccessToken.empty()));
    }

    @Test
    void testSavePageContent() throws Exception {
        // API response for the EditToken request
        String textResponse =
            "{\"batchcomplete\":true,\"query\":{\"pages\":[{\"pageid\":2209245,\"ns\":4,\"title\":\"Wikipedia:Zona de pruebas/5\",\"revisions\":[{\"timestamp\":\"2019-06-24T21:24:09Z\"}]}],\"tokens\":{\"csrftoken\":\"+\\\\\"}}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiRequestHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        // We use a timestamp AFTER the timestamp of the last edition (from the edit token)
        WikipediaTimestamp currentTimestamp = WikipediaTimestamp.of("2019-06-25T21:24:09Z");
        WikipediaPageSave pageSave = WikipediaPageSave
            .builder()
            .pageKey(PageKey.of(WikipediaLanguage.SPANISH, 1))
            .content("")
            .editSummary("")
            .queryTimestamp(currentTimestamp)
            .build();

        wikipediaPageRepository.save(pageSave, AccessToken.empty());

        // Two calls: one for the EditToken and another to save the content
        verify(wikipediaApiRequestHelper, times(2)).executeApiRequest(any(WikipediaApiRequest.class));

        // Save a section
        // We use a timestamp AFTER the timestamp of the last edition (from the edit token)
        currentTimestamp = WikipediaTimestamp.of("2019-06-26T21:24:09Z");
        pageSave =
            WikipediaPageSave
                .builder()
                .pageKey(PageKey.of(WikipediaLanguage.SPANISH, 1))
                .sectionId(2)
                .content("")
                .editSummary("")
                .queryTimestamp(currentTimestamp)
                .build();
        wikipediaPageRepository.save(pageSave, AccessToken.empty());

        // Two calls: one for the EditToken and another to save the content (x2 save page and section in this test)
        verify(wikipediaApiRequestHelper, times(4)).executeApiRequest(any(WikipediaApiRequest.class));
    }

    @Test
    void testBuildSearchExpressionCaseSensitive() {
        String text = "en Abril";
        String expected = "\"en Abril\" insource:/en Abril/";
        assertEquals(expected, wikipediaPageRepository.buildSearchExpression(text, true));
    }

    @Test
    void testBuildSearchExpressionCaseInsensitive() {
        String text = "en abril";
        String expected = "\"en abril\" insource:\"en abril\"";
        assertEquals(expected, wikipediaPageRepository.buildSearchExpression(text, false));
    }

    @Test
    void testGetPageSections() throws Exception {
        // API response
        String textResponse =
            "{\n" +
            "    \"parse\": {\n" +
            "        \"title\": \"Usuario:Benjavalero/Taller\",\n" +
            "        \"pageid\": 6903884,\n" +
            "        \"sections\": [\n" +
            "            {\n" +
            "                \"toclevel\": 1,\n" +
            "                \"level\": \"2\",\n" +
            "                \"line\": \"Pruebas con cursiva\",\n" +
            "                \"number\": \"1\",\n" +
            "                \"index\": \"1\",\n" +
            "                \"fromtitle\": \"Usuario:Benjavalero/Taller\",\n" +
            "                \"byteoffset\": 1998,\n" +
            "                \"anchor\": \"Pruebas_con_cursiva\",\n" +
            "                \"linkAnchor\": \"Pruebas_con_cursiva\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"toclevel\": 2,\n" +
            "                \"level\": \"3\",\n" +
            "                \"line\": \"Pruebas de subsecciones\",\n" +
            "                \"number\": \"1.1\",\n" +
            "                \"index\": \"2\",\n" +
            "                \"fromtitle\": \"Usuario:Benjavalero/Taller\",\n" +
            "                \"byteoffset\": 2275,\n" +
            "                \"anchor\": \"Pruebas_de_subsecciones\",\n" +
            "                \"linkAnchor\": \"Pruebas_de_subsecciones\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"toclevel\": 1,\n" +
            "                \"level\": \"2\",\n" +
            "                \"line\": \"Pruebas de banderas de la Selección Española\",\n" +
            "                \"number\": \"2\",\n" +
            "                \"index\": \"3\",\n" +
            "                \"fromtitle\": \"Usuario:Benjavalero/Taller\",\n" +
            "                \"byteoffset\": 2380,\n" +
            "                \"anchor\": \"Pruebas_de_banderas_de_la_Selección_Española\",\n" +
            "                \"linkAnchor\": \"Pruebas_de_banderas_de_la_Selección_Española\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"showtoc\": true\n" +
            "    }\n" +
            "}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiRequestHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        Collection<WikipediaSection> sections = wikipediaPageRepository.findSectionsInPage(
            PageKey.of(WikipediaLanguage.SPANISH, 6903884)
        );
        assertNotNull(sections);
        assertEquals(3, sections.size());

        assertTrue(sections.stream().anyMatch(sec -> sec.getIndex() == 1));
        assertEquals(
            1998,
            sections
                .stream()
                .filter(sec -> sec.getIndex() == 1)
                .findAny()
                .orElseThrow(WikipediaException::new)
                .getByteOffset()
        );

        assertTrue(sections.stream().anyMatch(sec -> sec.getIndex() == 2));
        assertEquals(
            2275,
            sections
                .stream()
                .filter(sec -> sec.getIndex() == 2)
                .findAny()
                .orElseThrow(WikipediaException::new)
                .getByteOffset()
        );

        assertTrue(sections.stream().anyMatch(sec -> sec.getIndex() == 3));
        assertEquals(
            2380,
            sections
                .stream()
                .filter(sec -> sec.getIndex() == 3)
                .findAny()
                .orElseThrow(WikipediaException::new)
                .getByteOffset()
        );

        assertTrue(sections.stream().allMatch(sec -> StringUtils.isNotEmpty(sec.getAnchor())));
    }

    @Test
    void testGetPageInvalidSections() throws Exception {
        // API response
        String textResponse =
            "{\n" +
            "    \"parse\": {\n" +
            "        \"title\": \"Anexo:Asteroides (161001)–(162000)\",\n" +
            "        \"pageid\": 6633556,\n" +
            "        \"sections\": [\n" +
            "            {\n" +
            "                \"toclevel\": 1,\n" +
            "                \"level\": \"2\",\n" +
            "                \"line\": \"Asteroides del (161001) al (161100)\",\n" +
            "                \"number\": \"1\",\n" +
            "                \"index\": \"\",\n" +
            "                \"fromtitle\": false,\n" +
            "                \"byteoffset\": null,\n" +
            "                \"anchor\": \"Asteroides_del_(161001)_al_(161100)\",\n" +
            "                \"linkAnchor\": \"Asteroides_del_(161001)_al_(161100)\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"toclevel\": 1,\n" +
            "                \"level\": \"2\",\n" +
            "                \"line\": \"Asteroides del (161101) al (161200)\",\n" +
            "                \"number\": \"2\",\n" +
            "                \"index\": \"\",\n" +
            "                \"fromtitle\": false,\n" +
            "                \"byteoffset\": null,\n" +
            "                \"anchor\": \"Asteroides_del_(161101)_al_(161200)\",\n" +
            "                \"linkAnchor\": \"Asteroides_del_(161101)_al_(161200)\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"toclevel\": 1,\n" +
            "                \"level\": \"2\",\n" +
            "                \"line\": \"Asteroides del (161201) al (161300)\",\n" +
            "                \"number\": \"3\",\n" +
            "                \"index\": \"\",\n" +
            "                \"fromtitle\": false,\n" +
            "                \"byteoffset\": null,\n" +
            "                \"anchor\": \"Asteroides_del_(161201)_al_(161300)\",\n" +
            "                \"linkAnchor\": \"Asteroides_del_(161201)_al_(161300)\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"showtoc\": true\n" +
            "    }\n" +
            "}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiRequestHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        Collection<WikipediaSection> sections = wikipediaPageRepository.findSectionsInPage(
            PageKey.of(WikipediaLanguage.SPANISH, 6633556)
        );
        assertNotNull(sections);
        assertTrue(sections.isEmpty());
    }

    @Test
    void testGetPageSection() throws Exception {
        // API response
        String textResponse =
            "{\n" +
            "    \"batchcomplete\": true,\n" +
            "    \"curtimestamp\": \"2023-01-19T09:04:19Z\",\n" +
            "    \"query\": {\n" +
            "        \"pages\": [\n" +
            "            {\n" +
            "                \"pageid\": 6903884,\n" +
            "                \"ns\": 2,\n" +
            "                \"title\": \"Usuario:Benjavalero/Taller\",\n" +
            "                \"contentmodel\": \"wikitext\",\n" +
            "                \"pagelanguage\": \"es\",\n" +
            "                \"pagelanguagehtmlcode\": \"es\",\n" +
            "                \"pagelanguagedir\": \"ltr\",\n" +
            "                \"touched\": \"2022-12-29T13:00:35Z\",\n" +
            "                \"lastrevid\": 147628235,\n" +
            "                \"length\": 8248,\n" +
            "                \"protection\": [],\n" +
            "                \"restrictiontypes\": [\n" +
            "                    \"edit\",\n" +
            "                    \"move\"\n" +
            "                ],\n" +
            "                \"revisions\": [\n" +
            "                    {\n" +
            "                        \"timestamp\": \"2022-11-29T15:45:09Z\",\n" +
            "                        \"slots\": {\n" +
            "                            \"main\": {\n" +
            "                                \"contentmodel\": \"wikitext\",\n" +
            "                                \"contentformat\": \"text/x-wiki\",\n" +
            "                                \"content\": \"== Pruebas con cursiva ==\\n\\n* El libro ''La historia interminable''.\\n* Comillas sin cerrar: ''La historia interminable\\n* Con negrita ''La '''historia''' interminable''.\\n* Con cursiva ''La ''historia'' interminable''.\\n* Con negrita buena ''La '''''historia''''' interminable''.\\n\\n=== Pruebas de subsecciones ===\\n\\nEsta es una subsección tonta solo para probar la captura de secciones.\"\n" +
            "                            }\n" +
            "                        }\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiRequestHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        PageKey pageKey = PageKey.of(WikipediaLanguage.getDefault(), 6903884);
        int sectionId = 1;
        WikipediaSection section = WikipediaSection
            .builder()
            .level(2)
            .index(sectionId)
            .byteOffset(0)
            .anchor("X")
            .build();
        String title = "Usuario:Benjavalero/Taller";
        WikipediaPage page = wikipediaPageRepository
            .findPageSection(pageKey, section)
            .orElseThrow(WikipediaException::new);
        assertNotNull(page);
        assertEquals(WikipediaLanguage.getDefault(), page.getPageKey().getLang());
        assertEquals(pageKey, page.getPageKey());
        assertEquals(title, page.getTitle());
        assertEquals(WikipediaNamespace.USER, page.getNamespace());
        assertTrue(page.getLastUpdate().toLocalDate().getYear() >= 2019);
        assertTrue(page.getContent().startsWith("=="));
    }

    @RetryingTest(maxAttempts = 3)
    void testWikipediaServiceOffline() {
        // Offline page
        Optional<WikipediaPage> page = wikipediaPageOfflineRepository.findByTitle(WikipediaLanguage.getDefault(), "");
        assertTrue(page.isPresent());
        int pageId = 1;
        page.ifPresent(p -> {
            assertEquals(pageId, p.getPageId());
            assertEquals(WikipediaNamespace.getDefault(), p.getNamespace());
            assertEquals("América del Norte", p.getTitle());
            assertFalse(p.isRedirect());

            Optional<WikipediaPage> page2 = wikipediaPageOfflineRepository.findByKey(
                PageKey.of(WikipediaLanguage.getDefault(), pageId)
            );
            // This test may fail sometimes if both fake offline pages have been built in different seconds
            // Just in case with annotate this test with @RetryingTest
            assertEquals(p, page2.orElse(null));

            Optional<WikipediaPage> pageSection = wikipediaPageOfflineRepository.findPageSection(
                PageKey.of(WikipediaLanguage.getDefault(), pageId),
                WikipediaSection.builder().anchor("").build()
            );
            assertEquals(p, pageSection.orElse(null));

            Collection<WikipediaPage> pages = wikipediaPageOfflineRepository.findByKeys(
                List.of(PageKey.of(WikipediaLanguage.getDefault(), pageId))
            );
            assertTrue(pages.isEmpty());
        });

        assertFalse(
            wikipediaPageOfflineRepository
                .findByContent(
                    WikipediaSearchRequest
                        .builder()
                        .lang(WikipediaLanguage.getDefault())
                        .namespaces(List.of(WikipediaNamespace.getDefault()))
                        .text("")
                        .offset(0)
                        .limit(100)
                        .build()
                )
                .isEmpty()
        );
        assertTrue(
            wikipediaPageOfflineRepository.findSectionsInPage(PageKey.of(WikipediaLanguage.getDefault(), 1)).isEmpty()
        );
    }
}
