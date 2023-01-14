package es.bvalero.replacer.wikipedia;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.util.WikipediaDateUtils;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.user.AccessToken;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequest;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequestHelper;
import es.bvalero.replacer.wikipedia.api.WikipediaApiResponse;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
            "{\"batchcomplete\":true,\"query\":{\"pages\":[{\"pageid\":2209245,\"ns\":4,\"title\":\"Wikipedia:Zona de pruebas/5\",\"revisions\":[{\"timestamp\":\"2019-06-24T21:24:09Z\"}]}],\"tokens\":{\"csrftoken\":\"+\\\\\"}}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiRequestHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);
        assertTrue(response.isBatchcomplete());

        // We pass an empty access token to retrieve an anonymous edit token
        PageKey pageKey = PageKey.of(WikipediaLanguage.SPANISH, 2209245);
        EditToken editToken = wikipediaPageRepository.getEditToken(pageKey, AccessToken.empty());
        assertNotNull(editToken.getCsrfToken());
        assertEquals("+\\", editToken.getCsrfToken());
        assertEquals("2019-06-24T21:24:09Z", WikipediaDateUtils.formatWikipediaTimestamp(editToken.getTimestamp()));
    }

    @Test
    void testGetPageContentByTitle() throws Exception {
        // API response
        String textResponse =
            "{\"batchcomplete\":true,\"curtimestamp\": \"2019-06-13T10:41:02Z\",\"query\":{\"pages\":[{\"pageid\":6219990,\"ns\":2,\"title\":\"Usuario:Benjavalero\",\"revisions\":[{\"timestamp\": \"2016-02-26T21:48:59Z\",\"slots\":{\"main\":{\"contentmodel\":\"wikitext\",\"contentformat\":\"text/x-wiki\",\"content\":\"Soy de [[Orihuela]]\"}}}]}]}}";
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
        assertTrue(page.getLastUpdate().getYear() >= 2016);
        assertTrue(page.getContent().contains("Orihuela"));
    }

    @Test
    void testGetPageContentById() throws Exception {
        // API response
        String textResponse =
            "{\"batchcomplete\":true,\"curtimestamp\": \"2019-06-13T10:41:02Z\",\"query\":{\"pages\":[{\"pageid\":6219990,\"ns\":2,\"title\":\"Usuario:Benjavalero\",\"revisions\":[{\"timestamp\": \"2016-02-26T21:48:59Z\",\"slots\":{\"main\":{\"contentmodel\":\"wikitext\",\"contentformat\":\"text/x-wiki\",\"content\":\"Soy de [[Orihuela]]\"}}}]}]}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiRequestHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        PageKey pageKey = PageKey.of(WikipediaLanguage.SPANISH, 6219990);
        String title = "Usuario:Benjavalero";
        WikipediaPage page = wikipediaPageRepository.findByKey(pageKey).orElseThrow(WikipediaException::new);
        assertNotNull(page);
        assertEquals(pageKey, page.getPageKey());
        assertEquals(title, page.getTitle());
        assertEquals(WikipediaNamespace.USER, page.getNamespace());
        assertTrue(page.getLastUpdate().getYear() >= 2016);
        assertTrue(page.getContent().contains("Orihuela"));
    }

    @Test
    void testGetPagesContent() throws Exception {
        // API response
        String textResponse =
            "{\"batchcomplete\":true,\"curtimestamp\": \"2019-06-13T10:41:02Z\",\"query\":{\"pages\":[{\"pageid\":6219990,\"ns\":2,\"title\":\"Usuario:Benjavalero\",\"revisions\":[{\"timestamp\": \"2016-02-26T21:48:59Z\",\"slots\":{\"main\":{\"contentmodel\":\"wikitext\",\"contentformat\":\"text/x-wiki\",\"content\":\"Soy de [[Orihuela]]\"}}}]},{\"pageid\":6903884,\"ns\":2,\"title\":\"Usuario:Benjavalero/Taller\",\"revisions\":[{\"timestamp\": \"2016-02-26T21:48:59Z\",\"slots\":{\"main\":{\"contentmodel\":\"wikitext\",\"contentformat\":\"text/x-wiki\",\"content\":\"Enlace a [[Pais Vasco]].\"}}}]}]}}";
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
            "{\"batchcomplete\":true,\"curtimestamp\": \"2019-06-13T10:41:02Z\",\"query\":{\"pages\":[{\"ns\":2,\"title\":\"Usuario:Benjavaleroxx\",\"missing\":true}]}}";
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
            "{\"batchcomplete\":true,\"curtimestamp\":\"2022-02-01T12:16:34Z\",\"query\":{\"pages\":[{\"pageid\":252086,\"ns\":0,\"title\":\"Getxo\",\"contentmodel\":\"wikitext\",\"pagelanguage\":\"es\",\"pagelanguagehtmlcode\":\"es\",\"pagelanguagedir\":\"ltr\",\"touched\":\"2021-12-29T16:47:23Z\",\"lastrevid\":8726615,\"length\":20,\"redirect\":true,\"protection\":[{\"type\":\"edit\",\"level\":\"sysop\",\"expiry\":\"infinity\"},{\"type\":\"move\",\"level\":\"sysop\",\"expiry\":\"infinity\"}],\"restrictiontypes\":[\"edit\",\"move\"],\"revisions\":[{\"timestamp\":\"2007-05-12T10:06:05Z\",\"slots\":{\"main\":{\"contentmodel\":\"wikitext\",\"contentformat\":\"text/x-wiki\",\"content\":\"#REDIRECT [[Guecho]]\"}}}]}]}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiRequestHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        assertFalse(wikipediaPageRepository.findByTitle(WikipediaLanguage.SPANISH, "Getxo").isPresent());
    }

    @Test
    void testGetPageIdsByStringMatch() throws Exception {
        // API response
        String textResponse =
            "{\"batchcomplete\":\"\",\"continue\":{\"sroffset\":100,\"continue\":\"-||\"},\"query\":{\"searchinfo\":{\"totalhits\":10},\"search\":[{\"ns\":0,\"title\":\"Belanova\",\"pageid\":297896},{\"ns\":0,\"title\":\"Wil Hartog\",\"pageid\":7694956},{\"ns\":0,\"title\":\"Compuesto químico\",\"pageid\":10547},{\"ns\":0,\"title\":\"Aun así te vas\",\"pageid\":2460037},{\"ns\":0,\"title\":\"Educación\",\"pageid\":975},{\"ns\":0,\"title\":\"Abolicionismo\",\"pageid\":173068},{\"ns\":0,\"title\":\"Canaán\",\"pageid\":718871},{\"ns\":0,\"title\":\"Coahuila de Zaragoza\",\"pageid\":724588},{\"ns\":0,\"title\":\"Filosofía\",\"pageid\":689592},{\"ns\":0,\"title\":\"Cárites\",\"pageid\":71433}]}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiRequestHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        WikipediaSearchResult pageIds = wikipediaPageRepository.findByContent(
            WikipediaLanguage.SPANISH,
            List.of(WikipediaNamespace.getDefault()),
            "",
            false,
            0,
            100
        );
        assertEquals(10, pageIds.getTotal());
    }

    @Test
    void testGetPageIdsByStringMatchWithNoResults() throws Exception {
        // API response
        String textResponse = "{\"batchcomplete\":\"\",\"query\":{\"searchinfo\":{\"totalhits\":0},\"search\":[]}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiRequestHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        WikipediaSearchResult pageIds = wikipediaPageRepository.findByContent(
            WikipediaLanguage.SPANISH,
            List.of(WikipediaNamespace.getDefault()),
            "",
            false,
            0,
            100
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
        LocalDateTime currentTimestamp = WikipediaDateUtils.parseWikipediaTimestamp("2019-06-23T21:24:09Z");

        assertThrows(
            WikipediaException.class,
            () ->
                wikipediaPageRepository.save(
                    PageKey.of(WikipediaLanguage.SPANISH, 1),
                    0,
                    "",
                    currentTimestamp,
                    "",
                    AccessToken.empty()
                )
        );
    }

    @Test
    void testSavePageContent() throws Exception {
        // API response for the EditToken request
        String textResponse =
            "{\"batchcomplete\":true,\"query\":{\"pages\":[{\"pageid\":2209245,\"ns\":4,\"title\":\"Wikipedia:Zona de pruebas/5\",\"revisions\":[{\"timestamp\":\"2019-06-24T21:24:09Z\"}]}],\"tokens\":{\"csrftoken\":\"+\\\\\"}}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiRequestHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        // We use a timestamp AFTER the timestamp of the last edition (from the edit token)
        LocalDateTime currentTimestamp = WikipediaDateUtils.parseWikipediaTimestamp("2019-06-25T21:24:09Z");
        wikipediaPageRepository.save(
            PageKey.of(WikipediaLanguage.SPANISH, 1),
            null,
            "",
            currentTimestamp,
            "",
            AccessToken.empty()
        );

        // Two calls: one for the EditToken and another to save the content
        verify(wikipediaApiRequestHelper, times(2)).executeApiRequest(any(WikipediaApiRequest.class));

        // Save a section
        // We use a timestamp AFTER the timestamp of the last edition (from the edit token)
        currentTimestamp = WikipediaDateUtils.parseWikipediaTimestamp("2019-06-26T21:24:09Z");
        wikipediaPageRepository.save(
            PageKey.of(WikipediaLanguage.SPANISH, 1),
            2,
            "",
            currentTimestamp,
            "",
            AccessToken.empty()
        );

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
            "{\"parse\":{\"title\":\"Usuario:Benjavalero/Taller\",\"pageid\":6903884,\"sections\":[{\"toclevel\":1,\"level\":\"2\",\"line\":\"Pruebas con cursiva\",\"number\":\"1\",\"index\":\"1\",\"fromtitle\":\"Usuario:Benjavalero/Taller\",\"byteoffset\":1998,\"anchor\":\"Pruebas_con_cursiva\"},{\"toclevel\":1,\"level\":\"2\",\"line\":\"Pruebas de banderas de la Selección Española\",\"number\":\"2\",\"index\":\"2\",\"fromtitle\":\"Usuario:Benjavalero/Taller\",\"byteoffset\":2275,\"anchor\":\"Pruebas_de_banderas_de_la_Selección_Española\"},{\"toclevel\":1,\"level\":\"2\",\"line\":\"Referencias\",\"number\":\"3\",\"index\":\"3\",\"fromtitle\":\"Usuario:Benjavalero/Taller\",\"byteoffset\":2497,\"anchor\":\"Referencias\"}], \"showtoc\": true}}";
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
            2497,
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
            "{\"parse\":{\"title\":\"Anexo:Asteroides (161001)\\u2013(162000)\",\"pageid\":6633556,\"sections\":[{\"toclevel\":1,\"level\":\"2\",\"line\":\"Asteroides del (161001) al (161100)\",\"number\":\"1\",\"index\":\"\",\"byteoffset\":null,\"anchor\":\"Asteroides_del_(161001)_al_(161100)\"},{\"toclevel\":1,\"level\":\"2\",\"line\":\"Asteroides del (161101) al (161200)\",\"number\":\"2\",\"index\":\"\",\"byteoffset\":null,\"anchor\":\"Asteroides_del_(161101)_al_(161200)\"}]}}";
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
            "{\"batchcomplete\":true,\"curtimestamp\":\"2019-10-17T15:12:03Z\",\"query\":{\"pages\":[{\"pageid\":6903884,\"ns\":2,\"title\":\"Usuario:Benjavalero/Taller\",\"revisions\":[{\"timestamp\":\"2019-08-24T07:51:05Z\",\"slots\":{\"main\":{\"contentmodel\":\"wikitext\",\"contentformat\":\"text/x-wiki\",\"content\":\"== Pruebas con cursiva ==\\n\\n* El libro ''La historia interminable''.\\n* Comillas sin cerrar: ''La historia interminable\\n* Con negrita ''La '''historia''' interminable''.\\n* Con cursiva ''La ''historia'' interminable''.\\n* Con negrita buena ''La '''''historia''''' interminable''.\\n\\n=== Pruebas de subsecciones ===\\n\\nEsta es una subsección tonta solo para probar la captura de secciones.\"}}}]}]}}";
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
        assertTrue(page.getLastUpdate().getYear() >= 2019);
        assertTrue(page.getContent().startsWith("=="));
    }

    @Test
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
                    WikipediaLanguage.getDefault(),
                    List.of(WikipediaNamespace.getDefault()),
                    "",
                    false,
                    0,
                    100
                )
                .isEmpty()
        );
        assertTrue(
            wikipediaPageOfflineRepository.findSectionsInPage(PageKey.of(WikipediaLanguage.getDefault(), 1)).isEmpty()
        );
    }
}