package es.bvalero.replacer.page.save;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.find.WikipediaTimestamp;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.api.WikipediaApiHelper;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequest;
import es.bvalero.replacer.wikipedia.api.WikipediaApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WikipediaPageSaveApiRepositoryTest {

    // Dependency injection
    private ObjectMapper jsonMapper;
    private WikipediaApiHelper wikipediaApiHelper;

    private WikipediaPageSaveApiRepository wikipediaPageSaveRepository;

    @BeforeEach
    void setUp() {
        jsonMapper = spy(ObjectMapper.class);
        wikipediaApiHelper = mock(WikipediaApiHelper.class);
        wikipediaPageSaveRepository = new WikipediaPageSaveApiRepository(wikipediaApiHelper);
    }

    @Test
    void testGetEditToken() throws Exception {
        // API response
        String textResponse =
            """
            {
                "batchcomplete": true,
                "query": {
                    "pages": [
                        {
                            "pageid": 2209245,
                            "ns": 4,
                            "title": "Wikipedia:Zona de pruebas/5",
                            "revisions": [
                                {
                                    "timestamp": "2023-01-18T20:40:07Z"
                                }
                            ]
                        }
                    ],
                    "tokens": {
                        "csrftoken": "332bb9c55bef953f93cb8391f8e6ee9e63c8fe90+\\\\"
                    }
                }
            }
            """;
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);
        assertTrue(response.isBatchcomplete());

        // We pass on purpose a null access token to retrieve an anonymous edit token
        PageKey pageKey = PageKey.of(WikipediaLanguage.SPANISH, 2209245);
        EditToken editToken = wikipediaPageSaveRepository.getEditToken(pageKey, null);
        assertNotNull(editToken.getCsrfToken());
        assertEquals("332bb9c55bef953f93cb8391f8e6ee9e63c8fe90+\\", editToken.getCsrfToken());
        assertEquals("2023-01-18T20:40:07Z", editToken.getTimestamp().toString());
    }

    @Test
    void testSavePageContentWithConflict() throws Exception {
        // API response for the EditToken request
        String textResponse =
            "{\"batchcomplete\":true,\"query\":{\"pages\":[{\"pageid\":2209245,\"ns\":4,\"title\":\"Wikipedia:Zona de pruebas/5\",\"revisions\":[{\"timestamp\":\"2019-06-24T21:24:09Z\"}]}],\"tokens\":{\"csrftoken\":\"+\\\\\"}}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        // We use a timestamp BEFORE the timestamp of the last edition (from the edit token)
        WikipediaTimestamp currentTimestamp = WikipediaTimestamp.of("2019-06-23T21:24:09Z");

        WikipediaPageSaveCommand pageSave = WikipediaPageSaveCommand.builder()
            .pageKey(PageKey.of(WikipediaLanguage.SPANISH, 1))
            .content("")
            .editSummary("")
            .queryTimestamp(currentTimestamp)
            .build();

        // We pass on purpose a null access token to perform an anonymous edit
        assertThrows(WikipediaException.class, () -> wikipediaPageSaveRepository.save(pageSave, null));
    }

    @Test
    void testSavePageContent() throws Exception {
        // API response for the EditToken request
        String editTokenJson =
            "{\"batchcomplete\":true,\"query\":{\"pages\":[{\"pageid\":2209245,\"ns\":4,\"title\":\"Wikipedia:Zona de pruebas/5\",\"revisions\":[{\"timestamp\":\"2019-06-24T21:24:09Z\"}]}],\"tokens\":{\"csrftoken\":\"+\\\\\"}}}";
        WikipediaApiResponse editTokenResponse = jsonMapper.readValue(editTokenJson, WikipediaApiResponse.class);
        String editResultJson =
            """
            {
                "edit": {
                    "result": "Success",
                    "pageid": 94542,
                    "title": "Wikipedia:Sandbox",
                    "contentmodel": "wikitext",
                    "oldrevid": 371705,
                    "newrevid": 371707,
                    "newtimestamp": "2018-12-18T16:59:42Z"
                }
            }
            """;
        WikipediaApiResponse editResultResponse = jsonMapper.readValue(editResultJson, WikipediaApiResponse.class);
        when(wikipediaApiHelper.executeApiRequest(any(WikipediaApiRequest.class)))
            .thenReturn(editTokenResponse)
            .thenReturn(editResultResponse)
            .thenReturn(editTokenResponse)
            .thenReturn(editResultResponse)
            .thenReturn(editTokenResponse)
            .thenReturn(editTokenResponse);

        // We use a timestamp AFTER the timestamp of the last edition (from the edit token)
        WikipediaTimestamp currentTimestamp = WikipediaTimestamp.of("2019-06-25T21:24:09Z");
        WikipediaPageSaveCommand pageSave = WikipediaPageSaveCommand.builder()
            .pageKey(PageKey.of(WikipediaLanguage.SPANISH, 1))
            .content("")
            .editSummary("")
            .queryTimestamp(currentTimestamp)
            .build();

        // We pass on purpose a null access token to perform an anonymous edit
        wikipediaPageSaveRepository.save(pageSave, null);

        // Two calls: one for the EditToken and another to save the content
        verify(wikipediaApiHelper, times(2)).executeApiRequest(any(WikipediaApiRequest.class));

        // Save a section
        // We use a timestamp AFTER the timestamp of the last edition (from the edit token)
        currentTimestamp = WikipediaTimestamp.of("2019-06-26T21:24:09Z");
        pageSave = WikipediaPageSaveCommand.builder()
            .pageKey(PageKey.of(WikipediaLanguage.SPANISH, 1))
            .sectionId(2)
            .content("")
            .editSummary("")
            .queryTimestamp(currentTimestamp)
            .build();

        // We pass on purpose a null access token to perform an anonymous edit
        wikipediaPageSaveRepository.save(pageSave, null);

        // Two calls: one for the EditToken and another to save the content (x2 save page and section in this test)
        verify(wikipediaApiHelper, times(4)).executeApiRequest(any(WikipediaApiRequest.class));
    }
}
