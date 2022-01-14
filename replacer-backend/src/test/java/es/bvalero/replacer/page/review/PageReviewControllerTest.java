package es.bvalero.replacer.page.review;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.util.WikipediaDateUtils;
import java.time.LocalDateTime;
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
@WebMvcTest(controllers = PageReviewController.class)
class PageReviewControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PageReviewNoTypeFinder pageReviewNoTypeFinder;

    @MockBean
    private PageReviewTypeSubtypeFinder pageReviewTypeSubtypeFinder;

    @MockBean
    private PageReviewCustomFinder pageReviewCustomFinder;

    @MockBean
    private ReplacementValidationService replacementValidationService;

    private final int pageId = 3;
    private final String title = "T";
    private final String content = "C";
    private final LocalDateTime queryTimestamp = LocalDateTime.now();
    private final WikipediaPage page = WikipediaPage
        .builder()
        .id(WikipediaPageId.of(WikipediaLanguage.getDefault(), pageId))
        .namespace(WikipediaNamespace.getDefault())
        .title(title)
        .content(content)
        .lastUpdate(LocalDateTime.now())
        .queryTimestamp(queryTimestamp)
        .build();
    private final int sectionId = 2;
    private final String anchor = "S";
    private final WikipediaSection section = WikipediaSection
        .builder()
        .level(2)
        .index(sectionId)
        .byteOffset(0)
        .anchor(anchor)
        .build();
    private final int start = 5;
    private final String rep = "A";
    private final Suggestion suggestion = Suggestion.of("a", "b");
    private final PageReplacement replacement = PageReplacement
        .builder()
        .start(start)
        .text(rep)
        .type(ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, rep))
        .suggestions(List.of(suggestion))
        .build();
    private final int numPending = 100;
    private final PageReview review = PageReview.of(page, section, List.of(replacement), numPending);

    @Test
    void testFindRandomPageWithReplacements() throws Exception {
        PageReviewOptions options = PageReviewOptions.ofNoType();
        when(pageReviewNoTypeFinder.findRandomPageReview(options)).thenReturn(Optional.of(review));

        mvc
            .perform(get("/api/pages/random?lang=es&user=A").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.id", is(pageId)))
            .andExpect(jsonPath("$.page.title", is(title)))
            .andExpect(jsonPath("$.page.content", is(content)))
            .andExpect(jsonPath("$.page.section.id", is(sectionId)))
            .andExpect(jsonPath("$.page.section.title", is(anchor)))
            .andExpect(
                jsonPath("$.page.queryTimestamp", is(WikipediaDateUtils.formatWikipediaTimestamp(queryTimestamp)))
            )
            .andExpect(jsonPath("$.replacements[0].start", is(start)))
            .andExpect(jsonPath("$.replacements[0].text", is(rep)))
            .andExpect(jsonPath("$.replacements[0].suggestions[0].text", is("a")))
            .andExpect(jsonPath("$.replacements[0].suggestions[0].comment", is("b")))
            .andExpect(jsonPath("$.numPending", is(numPending)));

        verify(pageReviewNoTypeFinder).findRandomPageReview(options);
    }

    @Test
    void testFindRandomPageByNoType() throws Exception {
        PageReviewOptions options = PageReviewOptions.ofNoType();
        when(pageReviewNoTypeFinder.findRandomPageReview(options)).thenReturn(Optional.of(review));

        mvc
            .perform(get("/api/pages/random?lang=es&user=A").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(pageReviewNoTypeFinder).findRandomPageReview(options);
    }

    @Test
    void testFindRandomPageByTypeAndSubtype() throws Exception {
        PageReviewOptions options = PageReviewOptions.ofType(ReplacementType.of(ReplacementKind.DATE, "Y"));
        when(pageReviewTypeSubtypeFinder.findRandomPageReview(options)).thenReturn(Optional.of(review));

        mvc
            .perform(
                get("/api/pages/random?type=Fechas&subtype=Y&lang=es&user=A").contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        verify(pageReviewTypeSubtypeFinder).findRandomPageReview(options);
    }

    @Test
    void testFindRandomPageByCustomReplacement() throws Exception {
        PageReviewOptions options = PageReviewOptions.ofCustom(WikipediaLanguage.SPANISH, "X", "Y", false);
        when(pageReviewCustomFinder.findRandomPageReview(options)).thenReturn(Optional.of(review));

        mvc
            .perform(
                get("/api/pages/random?type=Personalizado&subtype=X&cs=false&suggestion=Y&lang=es&user=A")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        verify(pageReviewCustomFinder).findRandomPageReview(options);
    }

    @Test
    void testFindPageReviewByIdWithWrongOptions() throws Exception {
        mvc
            .perform(get("/api/pages/123?type=X&lang=es&user=A").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verify(pageReviewNoTypeFinder, never()).getPageReview(anyInt(), any(PageReviewOptions.class));
    }

    @Test
    void testFindPageReviewById() throws Exception {
        PageReviewOptions options = PageReviewOptions.ofNoType();
        when(pageReviewNoTypeFinder.getPageReview(123, options)).thenReturn(Optional.of(review));

        mvc
            .perform(get("/api/pages/123?lang=es&user=A").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(pageReviewNoTypeFinder).getPageReview(123, options);
    }

    @Test
    void testFindPageReviewByIdByTypeAndSubtype() throws Exception {
        PageReviewOptions options = PageReviewOptions.ofType(ReplacementType.of(ReplacementKind.DATE, "Y"));
        when(pageReviewTypeSubtypeFinder.getPageReview(123, options)).thenReturn(Optional.of(review));

        mvc
            .perform(get("/api/pages/123?type=Fechas&subtype=Y&lang=es&user=A").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(pageReviewTypeSubtypeFinder).getPageReview(123, options);
    }

    @Test
    void testFindPageReviewByIdAndCustomReplacement() throws Exception {
        PageReviewOptions options = PageReviewOptions.ofCustom(WikipediaLanguage.SPANISH, "X", "Y", true);
        when(pageReviewCustomFinder.getPageReview(123, options)).thenReturn(Optional.of(review));

        mvc
            .perform(
                get("/api/pages/123?type=Personalizado&subtype=X&cs=true&suggestion=Y&lang=es&user=A")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        verify(pageReviewCustomFinder).getPageReview(123, options);
    }

    @Test
    void testValidateCustomReplacement() throws Exception {
        final String replacement = "Africa";
        when(replacementValidationService.findMatchingReplacementType(WikipediaLanguage.SPANISH, replacement, true))
            .thenReturn(Optional.of(ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, replacement)));

        mvc
            .perform(
                get("/api/pages/validate?replacement=Africa&cs=true&lang=es&user=A")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is(ReplacementKind.MISSPELLING_SIMPLE.getLabel())))
            .andExpect(jsonPath("$.subtype", is(replacement)));

        verify(replacementValidationService).findMatchingReplacementType(WikipediaLanguage.SPANISH, replacement, true);
    }
}
