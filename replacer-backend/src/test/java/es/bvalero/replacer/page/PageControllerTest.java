package es.bvalero.replacer.page;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinderService;
import es.bvalero.replacer.page.review.PageReviewSearch;
import es.bvalero.replacer.page.review.ReviewPage;
import es.bvalero.replacer.page.review.ReviewSection;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.wikipedia.WikipediaDateUtils;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = PageController.class)
class PageControllerTest {

    private static final String EMPTY_CONTENT = " ";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReplacementService replacementService;

    @MockBean
    private WikipediaService wikipediaService;

    @MockBean
    private CosmeticFinderService cosmeticFinderService;

    @MockBean
    private PageListService pageListService;

    @Test
    void testSaveWithChanges() throws Exception {
        int pageId = 123;
        int section = 3;
        String title = "Q";
        String content = "X";
        LocalDateTime timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        String token = "A";
        String tokenSecret = "B";
        String type = "T";
        String subtype = "S";
        SavePage savePage = new SavePage();
        ReviewSection reviewSection = new ReviewSection();
        reviewSection.setId(section);
        reviewSection.setTitle("");
        ReviewPage reviewPage = new ReviewPage();
        reviewPage.setLang(WikipediaLanguage.SPANISH);
        reviewPage.setId(pageId);
        reviewPage.setTitle(title);
        reviewPage.setContent(content);
        reviewPage.setSection(reviewSection);
        reviewPage.setQueryTimestamp(WikipediaDateUtils.formatWikipediaTimestamp(timestamp));
        savePage.setPage(reviewPage);
        PageReviewSearch search = new PageReviewSearch();
        search.setType(type);
        search.setSubtype(subtype);
        savePage.setSearch(search);
        savePage.setToken(token);
        savePage.setTokenSecret(tokenSecret);

        when(cosmeticFinderService.applyCosmeticChanges(any(FinderPage.class))).thenReturn("C");

        mvc
            .perform(
                post("/api/pages/123?lang=es&user=")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(savePage))
            )
            .andExpect(status().isOk());

        FinderPage finderPage = FinderPage.of(
            WikipediaLanguage.SPANISH,
            savePage.getPage().getContent(),
            savePage.getPage().getTitle()
        );
        verify(cosmeticFinderService).applyCosmeticChanges(finderPage);
        verify(wikipediaService)
            .savePageContent(
                eq(WikipediaPageId.of(WikipediaLanguage.SPANISH, pageId)),
                eq(section),
                eq("C"),
                eq(timestamp),
                anyString(),
                eq(AccessToken.of("A", "B"))
            );
    }

    @Test
    void testSaveWithNoChanges() throws Exception {
        int pageId = 123;
        int section = 3;
        String title = "Q";
        LocalDateTime timestamp = LocalDateTime.now();
        String token = "A";
        String tokenSecret = "B";
        String type = "T";
        String subtype = "S";
        SavePage savePage = new SavePage();
        ReviewSection reviewSection = new ReviewSection();
        reviewSection.setId(section);
        reviewSection.setTitle("");
        ReviewPage reviewPage = new ReviewPage();
        reviewPage.setLang(WikipediaLanguage.SPANISH);
        reviewPage.setId(pageId);
        reviewPage.setTitle(title);
        reviewPage.setContent(EMPTY_CONTENT);
        reviewPage.setSection(reviewSection);
        reviewPage.setQueryTimestamp(WikipediaDateUtils.formatWikipediaTimestamp(timestamp));
        savePage.setPage(reviewPage);
        PageReviewSearch search = new PageReviewSearch();
        search.setType(type);
        search.setSubtype(subtype);
        savePage.setSearch(search);
        savePage.setToken(token);
        savePage.setTokenSecret(tokenSecret);

        mvc
            .perform(
                post("/api/pages/123?lang=es&user=")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(savePage))
            )
            .andExpect(status().isOk());

        verify(cosmeticFinderService, times(0)).applyCosmeticChanges(any(FinderPage.class));
        verify(wikipediaService, times(0))
            .savePageContent(
                eq(WikipediaPageId.of(WikipediaLanguage.SPANISH, pageId)),
                anyInt(),
                anyString(),
                any(LocalDateTime.class),
                anyString(),
                any(AccessToken.class)
            );
    }
}
