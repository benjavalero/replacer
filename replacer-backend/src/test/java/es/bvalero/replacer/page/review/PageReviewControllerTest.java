package es.bvalero.replacer.page.review;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import es.bvalero.replacer.wikipedia.WikipediaDateUtils;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
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
    private PageReviewNoTypeService pageReviewNoTypeService;

    @MockBean
    private PageReviewTypeSubtypeService pageReviewTypeSubtypeService;

    @MockBean
    private PageReviewCustomService pageReviewCustomService;

    private final int pageId = 3;
    private final String title = "T";
    private final String content = "C";
    private final LocalDateTime queryTimestamp = LocalDateTime.now();
    private final WikipediaPage wikipediaPage = WikipediaPage
        .builder()
        .id(WikipediaPageId.of(WikipediaLanguage.SPANISH, pageId))
        .namespace(WikipediaNamespace.getDefault())
        .title(title)
        .content(content)
        .lastUpdate(LocalDateTime.now())
        .queryTimestamp(queryTimestamp)
        .build();
    private final PageReview review = PageReview.of(
        wikipediaPage,
        null,
        Collections.emptyList(),
        new PageReviewOptions(),
        100L
    );

    @Test
    void testFindRandomPageWithReplacements() throws Exception {
        // TODO: Check if all of this is needed
        Integer section = 2;
        String anchor = "S";
        int start = 5;
        String rep = "A";
        Suggestion suggestion = Suggestion.of("a", "b");
        Replacement replacement = Replacement
            .builder()
            .start(start)
            .text(rep)
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype(rep)
            .suggestions(Collections.singletonList(suggestion))
            .build();
        Collection<Replacement> replacements = Collections.singletonList(replacement);
        long numPending = 7;
        WikipediaSection wikipediaSection = WikipediaSection
            .builder()
            .level(2)
            .index(section)
            .byteOffset(0)
            .anchor(anchor)
            .build();
        PageReviewOptions options = PageReviewOptions.ofNoType();
        PageReview review = PageReview.of(wikipediaPage, wikipediaSection, replacements, options, numPending);
        when(pageReviewNoTypeService.findRandomPageReview(options)).thenReturn(Optional.of(review));

        mvc
            .perform(get("/api/pages/random?lang=es&user=").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.id", is(pageId)))
            .andExpect(jsonPath("$.page.title", is(title)))
            .andExpect(jsonPath("$.page.content", is(content)))
            .andExpect(jsonPath("$.page.section.id", is(section)))
            .andExpect(jsonPath("$.page.section.title", is(anchor)))
            .andExpect(
                jsonPath("$.page.queryTimestamp", is(WikipediaDateUtils.formatWikipediaTimestamp(queryTimestamp)))
            )
            .andExpect(jsonPath("$.replacements[0].start", is(start)))
            .andExpect(jsonPath("$.replacements[0].text", is(rep)))
            .andExpect(jsonPath("$.replacements[0].suggestions[0].text", is("a")))
            .andExpect(jsonPath("$.replacements[0].suggestions[0].comment", is("b")))
            .andExpect(jsonPath("$.numPending", is(Long.valueOf(numPending).intValue())));

        verify(pageReviewNoTypeService).findRandomPageReview(options);
    }

    @Test
    void testFindRandomPageByTypeAndSubtype() throws Exception {
        PageReviewOptions options = PageReviewOptions.ofTypeSubtype("X", "Y");
        when(pageReviewTypeSubtypeService.findRandomPageReview(options)).thenReturn(Optional.of(review));

        mvc
            .perform(get("/api/pages/random?type=X&subtype=Y&lang=es&user=").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(pageReviewTypeSubtypeService).findRandomPageReview(options);
    }

    @Test
    void testFindRandomPageByCustomReplacement() throws Exception {
        PageReviewOptions options = PageReviewOptions.ofCustom(WikipediaLanguage.SPANISH, "X", "Y", false);
        when(pageReviewCustomService.findRandomPageReview(options)).thenReturn(Optional.of(review));

        mvc
            .perform(
                get("/api/pages/random?type=Personalizado&subtype=X&cs=false&suggestion=Y&lang=es&user=")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        verify(pageReviewCustomService).findRandomPageReview(options);
    }

    @Test
    void testFindPageReviewById() throws Exception {
        PageReviewOptions options = PageReviewOptions.ofNoType();
        when(pageReviewNoTypeService.getPageReview(123, options)).thenReturn(Optional.of(review));

        mvc
            .perform(get("/api/pages/123?lang=es&user=").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(pageReviewNoTypeService).getPageReview(123, options);
    }

    @Test
    void testFindPageReviewByIdByTypeAndSubtype() throws Exception {
        PageReviewOptions options = PageReviewOptions.ofTypeSubtype("X", "Y");
        when(pageReviewTypeSubtypeService.getPageReview(123, options)).thenReturn(Optional.of(review));

        mvc
            .perform(get("/api/pages/123?type=X&subtype=Y&lang=es&user=").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(pageReviewTypeSubtypeService).getPageReview(123, options);
    }

    @Test
    void testFindPageReviewByIdAndCustomReplacement() throws Exception {
        PageReviewOptions options = PageReviewOptions.ofCustom(WikipediaLanguage.SPANISH, "X", "Y", true);
        when(pageReviewCustomService.getPageReview(123, options)).thenReturn(Optional.of(review));

        mvc
            .perform(
                get("/api/pages/123?type=Personalizado&subtype=X&cs=true&suggestion=Y&lang=es&user=")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        verify(pageReviewCustomService).getPageReview(123, options);
    }
}
