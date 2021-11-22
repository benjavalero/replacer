package es.bvalero.replacer.page;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinderService;
import es.bvalero.replacer.wikipedia.WikipediaDateUtils;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PageReviewNoTypeService pageReviewNoTypeService;

    @MockBean
    private PageReviewTypeSubtypeService pageReviewTypeSubtypeService;

    @MockBean
    private PageReviewCustomService pageReviewCustomService;

    @MockBean
    private WikipediaService wikipediaService;

    @MockBean
    private CosmeticFinderService cosmeticFinderService;

    @MockBean
    private PageListService pageListService;

    @Test
    void testFindRandomPageWithReplacements() throws Exception {
        int id = 3;
        String title = "X";
        String content = "Y";
        Integer section = 2;
        String anchor = "S";
        String queryTimestamp = "Z";
        int start = 5;
        String rep = "A";
        PageReplacementSuggestion suggestion = PageReplacementSuggestion.of("a", "b");
        PageReplacement replacement = PageReplacement.of(start, rep, Collections.singletonList(suggestion));
        List<PageReplacement> replacements = Collections.singletonList(replacement);
        long numPending = 7;
        PageReviewSearch search = PageReviewSearch.builder().numPending(numPending).build();
        PageDto page = new PageDto(
            WikipediaLanguage.SPANISH,
            id,
            title,
            content,
            PageSection.of(section, anchor),
            queryTimestamp
        );
        PageReview review = new PageReview(page, replacements, search);
        PageReviewOptions options = PageReviewOptions.ofNoType();
        when(pageReviewNoTypeService.findRandomPageReview(options)).thenReturn(Optional.of(review));

        mvc
            .perform(get("/api/pages/random?lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.id", is(id)))
            .andExpect(jsonPath("$.page.title", is(title)))
            .andExpect(jsonPath("$.page.content", is(content)))
            .andExpect(jsonPath("$.page.section.id", is(section)))
            .andExpect(jsonPath("$.page.section.title", is(anchor)))
            .andExpect(jsonPath("$.page.queryTimestamp", is(queryTimestamp)))
            .andExpect(jsonPath("$.replacements[0].start", is(start)))
            .andExpect(jsonPath("$.replacements[0].text", is(rep)))
            .andExpect(jsonPath("$.replacements[0].suggestions[0].text", is("a")))
            .andExpect(jsonPath("$.replacements[0].suggestions[0].comment", is("b")))
            .andExpect(jsonPath("$.search.numPending", is(Long.valueOf(numPending).intValue())));

        verify(pageReviewNoTypeService).findRandomPageReview(options);
    }

    @Test
    void testFindRandomPageByTypeAndSubtype() throws Exception {
        PageReviewOptions options = PageReviewOptions.ofTypeSubtype("X", "Y");
        when(pageReviewTypeSubtypeService.findRandomPageReview(options)).thenReturn(Optional.of(PageReview.ofEmpty()));

        mvc
            .perform(get("/api/pages/random?type=X&subtype=Y&lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(pageReviewTypeSubtypeService).findRandomPageReview(options);
    }

    @Test
    void testFindRandomPageByCustomReplacement() throws Exception {
        PageReviewOptions options = PageReviewOptions.ofCustom(WikipediaLanguage.SPANISH, "X", "Y", false);
        when(pageReviewCustomService.findRandomPageReview(options)).thenReturn(Optional.of(PageReview.ofEmpty()));

        mvc
            .perform(
                get("/api/pages/random?type=Personalizado&subtype=X&cs=false&suggestion=Y&lang=es")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        verify(pageReviewCustomService).findRandomPageReview(options);
    }

    @Test
    void testFindPageReviewById() throws Exception {
        PageReviewOptions options = PageReviewOptions.ofNoType();
        when(pageReviewNoTypeService.getPageReview(123, options)).thenReturn(Optional.of(PageReview.ofEmpty()));

        mvc.perform(get("/api/pages/123?lang=es").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        verify(pageReviewNoTypeService).getPageReview(123, options);
    }

    @Test
    void testFindPageReviewByIdByTypeAndSubtype() throws Exception {
        PageReviewOptions options = PageReviewOptions.ofTypeSubtype("X", "Y");
        when(pageReviewTypeSubtypeService.getPageReview(123, options)).thenReturn(Optional.of(PageReview.ofEmpty()));

        mvc
            .perform(get("/api/pages/123?type=X&subtype=Y&lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(pageReviewTypeSubtypeService).getPageReview(123, options);
    }

    @Test
    void testFindPageReviewByIdAndCustomReplacement() throws Exception {
        PageReviewOptions options = PageReviewOptions.ofCustom(WikipediaLanguage.SPANISH, "X", "Y", true);
        when(pageReviewCustomService.getPageReview(123, options)).thenReturn(Optional.of(PageReview.ofEmpty()));

        mvc
            .perform(
                get("/api/pages/123?type=Personalizado&subtype=X&cs=true&suggestion=Y&lang=es")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        verify(pageReviewCustomService).getPageReview(123, options);
    }

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
        PageDto page = PageDto
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .id(pageId)
            .title(title)
            .content(content)
            .section(PageSection.of(section, ""))
            .queryTimestamp(WikipediaDateUtils.formatWikipediaTimestamp(timestamp))
            .build();
        savePage.setPage(page);
        PageReviewSearch search = PageReviewSearch.builder().type(type).subtype(subtype).build();
        savePage.setSearch(search);
        savePage.setToken(token);
        savePage.setTokenSecret(tokenSecret);

        when(cosmeticFinderService.applyCosmeticChanges(any(FinderPage.class))).thenReturn("C");

        mvc
            .perform(
                post("/api/pages/123?lang=es")
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
        PageDto page = PageDto
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .id(pageId)
            .title(title)
            .section(PageSection.of(section, ""))
            .queryTimestamp(WikipediaDateUtils.formatWikipediaTimestamp(timestamp))
            .build();
        savePage.setPage(page);
        PageReviewSearch search = PageReviewSearch.builder().type(type).subtype(subtype).build();
        savePage.setSearch(search);
        savePage.setToken(token);
        savePage.setTokenSecret(tokenSecret);

        mvc
            .perform(
                post("/api/pages/123?lang=es")
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
