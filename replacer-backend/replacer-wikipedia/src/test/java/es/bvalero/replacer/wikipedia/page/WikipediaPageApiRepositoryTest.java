package es.bvalero.replacer.wikipedia.page;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.*;
import es.bvalero.replacer.wikipedia.api.WikipediaApiHelper;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequest;
import es.bvalero.replacer.wikipedia.api.WikipediaApiResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;

class WikipediaPageApiRepositoryTest {

    // Dependency injection
    private ObjectMapper jsonMapper;
    private WikipediaApiHelper wikipediaApiHelper;

    private WikipediaPageApiRepository wikipediaPageRepository;
    private WikipediaPageOfflineRepository wikipediaPageOfflineRepository;

    private final AccessToken accessToken = AccessToken.of("a", "b");

    @BeforeEach
    void setUp() {
        jsonMapper = spy(ObjectMapper.class);
        wikipediaApiHelper = mock(WikipediaApiHelper.class);
        wikipediaPageRepository = new WikipediaPageApiRepository(wikipediaApiHelper);
        wikipediaPageOfflineRepository = new WikipediaPageOfflineRepository();
    }

    @Test
    void testGetPageContentByTitle() throws Exception {
        // API response
        String textResponse =
            """
            {
                "batchcomplete": true,
                "curtimestamp": "2023-01-19T08:30:11Z",
                "query": {
                    "pages": [
                        {
                            "pageid": 6219990,
                            "ns": 2,
                            "title": "Usuario:Benjavalero",
                            "contentmodel": "wikitext",
                            "pagelanguage": "es",
                            "pagelanguagehtmlcode": "es",
                            "pagelanguagedir": "ltr",
                            "touched": "2023-01-18T11:59:55Z",
                            "lastrevid": 135919358,
                            "length": 971,
                            "revisions": [
                                {
                                    "timestamp": "2021-05-29T07:25:16Z",
                                    "slots": {
                                        "main": {
                                            "contentmodel": "wikitext",
                                            "contentformat": "text/x-wiki",
                                            "content": "Soy de [[Orihuela]]"
                                        }
                                    }
                                }
                            ]
                        }
                    ]
                }
            }
            """;
        InputStream textStream = new ByteArrayInputStream(textResponse.getBytes("UTF-8"));
        when(wikipediaApiHelper.executeApiRequestAsStream(any(WikipediaApiRequest.class))).thenReturn(textStream);

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
            """
            {
                "batchcomplete": true,
                "curtimestamp": "2023-01-19T08:35:51Z",
                "query": {
                    "pages": [
                        {
                            "pageid": 6219990,
                            "ns": 2,
                            "title": "Usuario:Benjavalero",
                            "contentmodel": "wikitext",
                            "pagelanguage": "es",
                            "pagelanguagehtmlcode": "es",
                            "pagelanguagedir": "ltr",
                            "touched": "2023-01-18T11:59:55Z",
                            "lastrevid": 135919358,
                            "length": 971,
                            "revisions": [
                                {
                                    "timestamp": "2021-05-29T07:25:16Z",
                                    "slots": {
                                        "main": {
                                            "contentmodel": "wikitext",
                                            "contentformat": "text/x-wiki",
                                            "content": "Soy de [[Orihuela]]"
                                        }
                                    }
                                }
                            ]
                        }
                    ]
                }
            }
            """;
        InputStream textStream = new ByteArrayInputStream(textResponse.getBytes("UTF-8"));
        when(wikipediaApiHelper.executeApiRequestAsStream(any(WikipediaApiRequest.class))).thenReturn(textStream);

        PageKey pageKey = PageKey.of(WikipediaLanguage.SPANISH, 6219990);
        String title = "Usuario:Benjavalero";
        WikipediaPage page = wikipediaPageRepository
            .findByKey(pageKey, accessToken)
            .orElseThrow(WikipediaException::new);
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
            """
            {
                "batchcomplete": true,
                "curtimestamp": "2023-01-19T08:37:13Z",
                "query": {
                    "pages": [
                        {
                            "pageid": 6219990,
                            "ns": 2,
                            "title": "Usuario:Benjavalero",
                            "contentmodel": "wikitext",
                            "pagelanguage": "es",
                            "pagelanguagehtmlcode": "es",
                            "pagelanguagedir": "ltr",
                            "touched": "2023-01-18T11:59:55Z",
                            "lastrevid": 135919358,
                            "length": 971,
                            "revisions": [
                                {
                                    "timestamp": "2021-05-29T07:25:16Z",
                                    "slots": {
                                        "main": {
                                            "contentmodel": "wikitext",
                                            "contentformat": "text/x-wiki",
                                            "content": "Soy de [[Orihuela]]"
                                        }
                                    }
                                }
                            ]
                        },
                        {
                            "pageid": 6903884,
                            "ns": 2,
                            "title": "Usuario:Benjavalero/Taller",
                            "contentmodel": "wikitext",
                            "pagelanguage": "es",
                            "pagelanguagehtmlcode": "es",
                            "pagelanguagedir": "ltr",
                            "touched": "2022-12-29T13:00:35Z",
                            "lastrevid": 147628235,
                            "length": 8248,
                            "revisions": [
                                {
                                    "timestamp": "2022-11-29T15:45:09Z",
                                    "slots": {
                                        "main": {
                                            "contentmodel": "wikitext",
                                            "contentformat": "text/x-wiki",
                                            "content": "Enlace a [[Pais Vasco]]."
                                        }
                                    }
                                }
                            ]
                        }
                    ]
                }
            }
            """;
        InputStream textStream = new ByteArrayInputStream(textResponse.getBytes("UTF-8"));
        when(wikipediaApiHelper.executeApiRequestAsStream(any(WikipediaApiRequest.class))).thenReturn(textStream);

        WikipediaLanguage lang = WikipediaLanguage.SPANISH;
        Collection<WikipediaPage> pages = wikipediaPageRepository
            .findByKeys(List.of(PageKey.of(lang, 6219990), PageKey.of(lang, 6903884)), accessToken)
            .toList();
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
            """
            {
                "batchcomplete": true,
                "curtimestamp": "2023-01-19T08:38:51Z",
                "query": {
                    "pages": [
                        {
                            "ns": 2,
                            "title": "Usuario:Benjavaleroxx",
                            "missing": true,
                            "contentmodel": "wikitext",
                            "pagelanguage": "es",
                            "pagelanguagehtmlcode": "es",
                            "pagelanguagedir": "ltr"
                        }
                    ]
                }
            }
            """;
        InputStream textStream = new ByteArrayInputStream(textResponse.getBytes("UTF-8"));
        when(wikipediaApiHelper.executeApiRequestAsStream(any(WikipediaApiRequest.class))).thenReturn(textStream);

        assertFalse(
            wikipediaPageRepository.findByTitle(WikipediaLanguage.SPANISH, "Usuario:Benjavaleroxx").isPresent()
        );
    }

    @Test
    void testGetProtectedPage() throws Exception {
        // API response
        String textResponse =
            """
            {
                "batchcomplete": true,
                "curtimestamp": "2023-01-19T09:07:39Z",
                "query": {
                    "pages": [
                        {
                            "pageid": 252086,
                            "ns": 0,
                            "title": "Getxo",
                            "contentmodel": "wikitext",
                            "pagelanguage": "es",
                            "pagelanguagehtmlcode": "es",
                            "pagelanguagedir": "ltr",
                            "touched": "2023-01-10T23:32:55Z",
                            "lastrevid": 8726615,
                            "length": 20,
                            "redirect": true,
                            "protection": [
                                {
                                    "type": "edit",
                                    "level": "sysop",
                                    "expiry": "infinity"
                                },
                                {
                                    "type": "move",
                                    "level": "sysop",
                                    "expiry": "infinity"
                                }
                            ],
                            "restrictiontypes": [
                                "edit",
                                "move"
                            ],
                            "revisions": [
                                {
                                    "timestamp": "2007-05-12T10:06:05Z",
                                    "slots": {
                                        "main": {
                                            "contentmodel": "wikitext",
                                            "contentformat": "text/x-wiki",
                                            "content": "#REDIRECT [[Guecho]]"
                                        }
                                    }
                                }
                            ]
                        }
                    ]
                }
            }
            """;
        InputStream textStream = new ByteArrayInputStream(textResponse.getBytes("UTF-8"));
        when(wikipediaApiHelper.executeApiRequestAsStream(any(WikipediaApiRequest.class))).thenReturn(textStream);

        assertFalse(wikipediaPageRepository.findByTitle(WikipediaLanguage.SPANISH, "Getxo").isPresent());
    }

    @Test
    void testGetPageIdsByStringMatch() throws Exception {
        // API response
        String textResponse =
            """
            {
                "batchcomplete": true,
                "query": {
                    "searchinfo": {
                        "totalhits": 13
                    },
                    "search": [
                        {
                            "ns": 104,
                            "title": "Anexo:Municipios y comarcas de las Islas Baleares",
                            "pageid": 29088
                        },
                        {
                            "ns": 104,
                            "title": "Anexo:Red de Carreteras de la Comunidad Valenciana",
                            "pageid": 637929
                        },
                        {
                            "ns": 104,
                            "title": "Anexo:Armorial municipal de las Islas Baleares",
                            "pageid": 1418604
                        },
                        {
                            "ns": 104,
                            "title": "Anexo:Cobertura de las Demarcaciones Locales de TDT",
                            "pageid": 2365909
                        },
                        {
                            "ns": 104,
                            "title": "Anexo:Partidos judiciales de las Islas Baleares",
                            "pageid": 4489346
                        },
                        {
                            "ns": 104,
                            "title": "Anexo:Bienes de interés cultural de las Islas Baleares",
                            "pageid": 5390126
                        },
                        {
                            "ns": 104,
                            "title": "Anexo:The Amazing Race 21",
                            "pageid": 5397284
                        },
                        {
                            "ns": 104,
                            "title": "Anexo:Bienes de relevancia local de la Marina Baja",
                            "pageid": 8684602
                        },
                        {
                            "ns": 104,
                            "title": "Anexo:Monumentos de la comarca de la Marina Baja",
                            "pageid": 8753312
                        },
                        {
                            "ns": 104,
                            "title": "Anexo:Monumentos de la ciudad de Valencia (A-L)",
                            "pageid": 8754198
                        },
                        {
                            "ns": 104,
                            "title": "Anexo:Monumentos de la ciudad de Valencia (M-Z)",
                            "pageid": 8755195
                        },
                        {
                            "ns": 104,
                            "title": "Anexo:Monumentos del distrito de Poblados del Sur",
                            "pageid": 8757203
                        },
                        {
                            "ns": 104,
                            "title": "Anexo:Velódromos de Mallorca",
                            "pageid": 9436640
                        }
                    ]
                }
            }
            """;
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        WikipediaSearchResult pageIds = wikipediaPageRepository.findByContent(
            WikipediaSearchRequest.builder()
                .lang(WikipediaLanguage.SPANISH)
                .namespaces(List.of(WikipediaNamespace.ANNEX))
                .text("Campaneta")
                .build(),
            accessToken
        );
        assertEquals(13, pageIds.getTotal());
    }

    @Test
    void testGetPageIdsByStringMatchWithNoResults() throws Exception {
        // API response
        String textResponse =
            """
            {
                "batchcomplete": true,
                "query": {
                    "searchinfo": {
                        "totalhits": 0
                    },
                    "search": []
                }
            }
            """;
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        WikipediaSearchResult pageIds = wikipediaPageRepository.findByContent(
            WikipediaSearchRequest.builder().lang(WikipediaLanguage.SPANISH).text("jsdfslkdfjhow").build(),
            accessToken
        );
        assertTrue(pageIds.isEmpty());
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
            """
            {
                "parse": {
                    "title": "Usuario:Benjavalero/Taller",
                    "pageid": 6903884,
                    "sections": [
                        {
                            "toclevel": 1,
                            "level": "2",
                            "line": "Pruebas con cursiva",
                            "number": "1",
                            "index": "1",
                            "fromtitle": "Usuario:Benjavalero/Taller",
                            "byteoffset": 1998,
                            "anchor": "Pruebas_con_cursiva",
                            "linkAnchor": "Pruebas_con_cursiva"
                        },
                        {
                            "toclevel": 2,
                            "level": "3",
                            "line": "Pruebas de subsecciones",
                            "number": "1.1",
                            "index": "2",
                            "fromtitle": "Usuario:Benjavalero/Taller",
                            "byteoffset": 2275,
                            "anchor": "Pruebas_de_subsecciones",
                            "linkAnchor": "Pruebas_de_subsecciones"
                        },
                        {
                            "toclevel": 1,
                            "level": "2",
                            "line": "Pruebas de banderas de la Selección Española",
                            "number": "2",
                            "index": "3",
                            "fromtitle": "Usuario:Benjavalero/Taller",
                            "byteoffset": 2380,
                            "anchor": "Pruebas_de_banderas_de_la_Selección_Española",
                            "linkAnchor": "Pruebas_de_banderas_de_la_Selección_Española"
                        }
                    ],
                    "showtoc": true
                }
            }
            """;
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        Collection<WikipediaSection> sections = wikipediaPageRepository.findSectionsInPage(
            PageKey.of(WikipediaLanguage.SPANISH, 6903884),
            accessToken
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
            """
            {
                "parse": {
                    "title": "Anexo:Asteroides (161001)–(162000)",
                    "pageid": 6633556,
                    "sections": [
                        {
                            "toclevel": 1,
                            "level": "2",
                            "line": "Asteroides del (161001) al (161100)",
                            "number": "1",
                            "index": "",
                            "fromtitle": false,
                            "byteoffset": null,
                            "anchor": "Asteroides_del_(161001)_al_(161100)",
                            "linkAnchor": "Asteroides_del_(161001)_al_(161100)"
                        },
                        {
                            "toclevel": 1,
                            "level": "2",
                            "line": "Asteroides del (161101) al (161200)",
                            "number": "2",
                            "index": "",
                            "fromtitle": false,
                            "byteoffset": null,
                            "anchor": "Asteroides_del_(161101)_al_(161200)",
                            "linkAnchor": "Asteroides_del_(161101)_al_(161200)"
                        },
                        {
                            "toclevel": 1,
                            "level": "2",
                            "line": "Asteroides del (161201) al (161300)",
                            "number": "3",
                            "index": "",
                            "fromtitle": false,
                            "byteoffset": null,
                            "anchor": "Asteroides_del_(161201)_al_(161300)",
                            "linkAnchor": "Asteroides_del_(161201)_al_(161300)"
                        }
                    ],
                    "showtoc": true
                }
            }
            """;
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        Collection<WikipediaSection> sections = wikipediaPageRepository.findSectionsInPage(
            PageKey.of(WikipediaLanguage.SPANISH, 6633556),
            accessToken
        );
        assertNotNull(sections);
        assertTrue(sections.isEmpty());
    }

    @Test
    void testGetPageSection() throws Exception {
        // API response
        String textResponse =
            """
            {
                "batchcomplete": true,
                "curtimestamp": "2023-01-19T09:04:19Z",
                "query": {
                    "pages": [
                        {
                            "pageid": 6903884,
                            "ns": 2,
                            "title": "Usuario:Benjavalero/Taller",
                            "contentmodel": "wikitext",
                            "pagelanguage": "es",
                            "pagelanguagehtmlcode": "es",
                            "pagelanguagedir": "ltr",
                            "touched": "2022-12-29T13:00:35Z",
                            "lastrevid": 147628235,
                            "length": 8248,
                            "protection": [],
                            "restrictiontypes": [
                                "edit",
                                "move"
                            ],
                            "revisions": [
                                {
                                    "timestamp": "2022-11-29T15:45:09Z",
                                    "slots": {
                                        "main": {
                                            "contentmodel": "wikitext",
                                            "contentformat": "text/x-wiki",
                                            "content": "== Pruebas con cursiva ==\\n\\n* El libro ''La historia interminable''.\\n* Comillas sin cerrar: ''La historia interminable\\n* Con negrita ''La '''historia''' interminable''.\\n* Con cursiva ''La ''historia'' interminable''.\\n* Con negrita buena ''La '''''historia''''' interminable''.\\n\\n=== Pruebas de subsecciones ===\\n\\nEsta es una subsección tonta solo para probar la captura de secciones."
                                        }
                                    }
                                }
                            ]
                        }
                    ]
                }
            }
            """;
        InputStream textStream = new ByteArrayInputStream(textResponse.getBytes("UTF-8"));
        when(wikipediaApiHelper.executeApiRequestAsStream(any(WikipediaApiRequest.class))).thenReturn(textStream);

        PageKey pageKey = PageKey.of(WikipediaLanguage.getDefault(), 6903884);
        int sectionId = 1;
        WikipediaSection section = WikipediaSection.builder()
            .pageKey(pageKey)
            .index(sectionId)
            .level(2)
            .byteOffset(0)
            .anchor("X")
            .build();
        String title = "Usuario:Benjavalero/Taller";
        WikipediaPage page = wikipediaPageRepository
            .findPageSection(section, accessToken)
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
                PageKey.of(WikipediaLanguage.getDefault(), pageId),
                accessToken
            );
            // This test may fail sometimes if both fake offline pages have been built in different seconds
            // Just in case with annotate this test with @RetryingTest
            assertEquals(p, page2.orElse(null));

            PageKey pageKey = PageKey.of(WikipediaLanguage.getDefault(), pageId);
            Optional<WikipediaPage> pageSection = wikipediaPageOfflineRepository.findPageSection(
                WikipediaSection.builder().pageKey(pageKey).anchor("").build(),
                accessToken
            );
            assertEquals(p, pageSection.orElse(null));
        });

        assertFalse(
            wikipediaPageOfflineRepository
                .findByContent(
                    WikipediaSearchRequest.builder().lang(WikipediaLanguage.getDefault()).text("").build(),
                    accessToken
                )
                .isEmpty()
        );
        assertTrue(
            wikipediaPageOfflineRepository
                .findSectionsInPage(PageKey.of(WikipediaLanguage.getDefault(), 1), accessToken)
                .isEmpty()
        );
    }
}
