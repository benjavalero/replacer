package es.bvalero.replacer.page;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.authentication.AccessToken;
import es.bvalero.replacer.finder.CosmeticFindService;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.replacement.ReplacementCountService;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaService;
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
    private CosmeticFindService cosmeticFindService;

    @MockBean
    private PageListService pageListService;

    @MockBean
    private ReplacementCountService replacementCountService;

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
        Suggestion suggestion = Suggestion.of("a", "b");
        PageReplacement replacement = new PageReplacement(start, rep, Collections.singletonList(suggestion));
        List<PageReplacement> replacements = Collections.singletonList(replacement);
        long numPending = replacements.size();
        PageReview review = new PageReview(
            id,
            WikipediaLanguage.SPANISH,
            title,
            content,
            section,
            anchor,
            queryTimestamp,
            replacements,
            numPending
        );
        PageReviewOptions options = PageReviewOptions.ofNoType(WikipediaLanguage.SPANISH);
        when(pageReviewNoTypeService.findRandomPageReview(options)).thenReturn(Optional.of(review));

        mvc
            .perform(get("/api/pages/random?lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(id)))
            .andExpect(jsonPath("$.title", is(title)))
            .andExpect(jsonPath("$.content", is(content)))
            .andExpect(jsonPath("$.section", is(section)))
            .andExpect(jsonPath("$.anchor", is(anchor)))
            .andExpect(jsonPath("$.queryTimestamp", is(queryTimestamp)))
            .andExpect(jsonPath("$.replacements[0].start", is(start)))
            .andExpect(jsonPath("$.replacements[0].text", is(rep)))
            .andExpect(jsonPath("$.replacements[0].suggestions[0].text", is("a")))
            .andExpect(jsonPath("$.replacements[0].suggestions[0].comment", is("b")))
            .andExpect(jsonPath("$.numPending", is(Long.valueOf(numPending).intValue())));

        verify(pageReviewNoTypeService, times(1)).findRandomPageReview(options);
    }

    @Test
    void testFindRandomPageByTypeAndSubtype() throws Exception {
        PageReviewOptions options = PageReviewOptions.ofTypeSubtype(WikipediaLanguage.SPANISH, "X", "Y");
        when(pageReviewTypeSubtypeService.findRandomPageReview(options)).thenReturn(Optional.of(new PageReview()));

        mvc
            .perform(get("/api/pages/random?type=X&subtype=Y&lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(pageReviewTypeSubtypeService, times(1)).findRandomPageReview(eq(options));
    }

    @Test
    void testFindRandomPageByCustomReplacement() throws Exception {
        PageReviewOptions options = PageReviewOptions.ofCustom(WikipediaLanguage.SPANISH, "X", "Y");
        when(pageReviewCustomService.findRandomPageReview(options)).thenReturn(Optional.of(new PageReview()));

        mvc
            .perform(
                get("/api/pages/random?replacement=X&suggestion=Y&lang=es").contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        verify(pageReviewCustomService, times(1)).findRandomPageReview(eq(options));
    }

    @Test
    void testFindPageReviewById() throws Exception {
        PageReviewOptions options = PageReviewOptions.ofNoType(WikipediaLanguage.SPANISH);
        when(pageReviewNoTypeService.getPageReview(123, options)).thenReturn(Optional.of(new PageReview()));

        mvc.perform(get("/api/pages/123?lang=es").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        verify(pageReviewNoTypeService, times(1)).getPageReview(123, options);
    }

    @Test
    void testFindPageReviewByIdByTypeAndSubtype() throws Exception {
        PageReviewOptions options = PageReviewOptions.ofTypeSubtype(WikipediaLanguage.SPANISH, "X", "Y");
        when(pageReviewTypeSubtypeService.getPageReview(123, options)).thenReturn(Optional.of(new PageReview()));

        mvc
            .perform(get("/api/pages/123?type=X&subtype=Y&lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(pageReviewTypeSubtypeService, times(1)).getPageReview(123, options);
    }

    @Test
    void testFindPageReviewByIdAndCustomReplacement() throws Exception {
        PageReviewOptions options = PageReviewOptions.ofCustom(WikipediaLanguage.SPANISH, "X", "Y");
        when(pageReviewCustomService.getPageReview(123, options)).thenReturn(Optional.of(new PageReview()));

        mvc
            .perform(get("/api/pages/123?replacement=X&suggestion=Y&lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(pageReviewCustomService, times(1)).getPageReview(123, options);
    }

    @Test
    void testSaveWithChanges() throws Exception {
        int pageId = 123;
        int section = 3;
        String title = "Q";
        String content = "X";
        String timestamp = "Y";
        String reviewer = "Z";
        AccessToken token = new AccessToken("A", "B");
        String type = "T";
        String subtype = "S";
        SavePage savePage = new SavePage(section, title, content, timestamp, reviewer, token, type, subtype);

        when(cosmeticFindService.applyCosmeticChanges(any(WikipediaPage.class))).thenReturn("C");

        mvc
            .perform(
                post("/api/pages/123?lang=es")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(savePage))
            )
            .andExpect(status().isOk());

        WikipediaPage page = WikipediaPage
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .content(savePage.getContent())
            .title(savePage.getTitle())
            .build();
        verify(cosmeticFindService, times(1)).applyCosmeticChanges(eq(page));
        verify(wikipediaService, times(1))
            .savePageContent(
                any(WikipediaLanguage.class),
                eq(pageId),
                eq(section),
                eq("C"),
                eq(timestamp),
                eq(new OAuth1AccessToken("A", "B"))
            );
    }

    @Test
    void testSaveWithNoChanges() throws Exception {
        int pageId = 123;
        int section = 3;
        String title = "Q";
        String timestamp = "Y";
        String reviewer = "Z";
        AccessToken token = new AccessToken("A", "B");
        String type = "T";
        String subtype = "S";
        SavePage savePage = new SavePage(section, title, null, timestamp, reviewer, token, type, subtype);

        mvc
            .perform(
                post("/api/pages/123?lang=es")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(savePage))
            )
            .andExpect(status().isOk());

        verify(cosmeticFindService, times(0)).applyCosmeticChanges(any(WikipediaPage.class));
        verify(wikipediaService, times(0))
            .savePageContent(
                any(WikipediaLanguage.class),
                eq(pageId),
                anyInt(),
                anyString(),
                anyString(),
                any(OAuth1AccessToken.class)
            );
    }
}
