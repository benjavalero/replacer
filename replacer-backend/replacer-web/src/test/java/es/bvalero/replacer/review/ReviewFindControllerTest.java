package es.bvalero.replacer.review;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.user.UserId;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaSection;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ReviewFindController.class)
class ReviewFindControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewNoTypeFinder reviewNoTypeFinder;

    @MockBean
    private ReviewTypeFinder reviewTypeFinder;

    @MockBean
    private ReviewCustomFinder reviewCustomFinder;

    private final int pageId = 3;
    private final String title = "T";
    private final String content = "ABCDE";
    private final WikipediaTimestamp now = WikipediaTimestamp.now();
    private final WikipediaTimestamp queryTimestamp = now;
    private final WikipediaPage page = WikipediaPage
        .builder()
        .pageKey(PageKey.of(WikipediaLanguage.getDefault(), pageId))
        .namespace(WikipediaNamespace.getDefault())
        .title(title)
        .content(content)
        .lastUpdate(now)
        .queryTimestamp(queryTimestamp)
        .build();
    private final int sectionId = 2;
    private final String anchor = "S";
    private final WikipediaSection section = WikipediaSection
        .builder()
        .pageKey(page.getPageKey())
        .index(sectionId)
        .level(2)
        .byteOffset(0)
        .anchor(anchor)
        .build();
    private final int start = 2;
    private final String rep = "C";
    private final Suggestion suggestion = Suggestion.of("c", "ç");
    private final Replacement replacement = Replacement
        .builder()
        .start(start)
        .text(rep)
        .type(StandardType.of(ReplacementKind.SIMPLE, rep))
        .suggestions(List.of(suggestion))
        .build();
    private final int numPending = 100;
    private final Review review = Review.of(page, section, List.of(replacement), numPending);

    @Test
    void testFindRandomPageWithReplacements() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "A");
        ReviewOptions options = ReviewOptions.ofNoType(userId);
        when(reviewNoTypeFinder.findRandomPageReview(options)).thenReturn(Optional.of(review));

        mvc
            .perform(
                get("/api/review/random?user=A")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.pageId", is(pageId)))
            .andExpect(jsonPath("$.page.title", is(title)))
            .andExpect(jsonPath("$.page.content", is(content)))
            .andExpect(jsonPath("$.page.section.id", is(sectionId)))
            .andExpect(jsonPath("$.page.section.title", is(anchor)))
            .andExpect(jsonPath("$.page.queryTimestamp", is(queryTimestamp.toString())))
            .andExpect(jsonPath("$.replacements[0].start", is(start)))
            .andExpect(jsonPath("$.replacements[0].text", is(rep)))
            .andExpect(jsonPath("$.replacements[0].suggestions[0].text", is(rep)))
            .andExpect(jsonPath("$.replacements[0].suggestions[0].comment").doesNotExist())
            .andExpect(jsonPath("$.replacements[0].suggestions[1].text", is(suggestion.getText())))
            .andExpect(jsonPath("$.replacements[0].suggestions[1].comment", is(suggestion.getComment())))
            .andExpect(jsonPath("$.numPending", is(numPending)));

        verify(reviewNoTypeFinder).findRandomPageReview(options);
    }

    @Test
    void testFindRandomPageByNoType() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "A");
        ReviewOptions options = ReviewOptions.ofNoType(userId);
        when(reviewNoTypeFinder.findRandomPageReview(options)).thenReturn(Optional.of(review));

        mvc
            .perform(
                get("/api/review/random?user=A")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        verify(reviewNoTypeFinder).findRandomPageReview(options);
    }

    @Test
    void testFindRandomPageByTypeAndSubtype() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "A");
        StandardType type = StandardType.DATE;
        ReviewOptions options = ReviewOptions.ofType(userId, type);
        when(reviewTypeFinder.findRandomPageReview(options)).thenReturn(Optional.of(review));

        mvc
            .perform(
                get("/api/review/random?kind=5&subtype=Fechas&user=A")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        verify(reviewTypeFinder).findRandomPageReview(options);
    }

    @Test
    void testFindRandomPageByCustomReplacement() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "A");
        CustomType customType = CustomType.of("X", false, "Y");
        ReviewOptions options = ReviewOptions.ofCustom(userId, customType);
        when(reviewCustomFinder.findRandomPageReview(options)).thenReturn(Optional.of(review));

        mvc
            .perform(
                get("/api/review/random?kind=1&subtype=X&cs=false&suggestion=Y&user=A")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        verify(reviewCustomFinder).findRandomPageReview(options);
    }

    @Test
    void testFindPageReviewByIdWithWrongOptions() throws Exception {
        mvc
            .perform(
                get("/api/review/123?kind=X&user=A")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());

        verify(reviewNoTypeFinder, never()).findPageReview(any(PageKey.class), any(ReviewOptions.class));
    }

    @Test
    void testFindPageReviewById() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "A");
        ReviewOptions options = ReviewOptions.ofNoType(userId);
        PageKey pageKey = PageKey.of(WikipediaLanguage.getDefault(), 123);
        when(reviewNoTypeFinder.findPageReview(pageKey, options)).thenReturn(Optional.of(review));

        mvc
            .perform(
                get("/api/review/123?user=A")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        verify(reviewNoTypeFinder).findPageReview(pageKey, options);
    }

    @Test
    void testFindPageReviewByIdByTypeAndSubtype() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "A");
        StandardType type = StandardType.DATE;
        ReviewOptions options = ReviewOptions.ofType(userId, type);
        PageKey pageKey = PageKey.of(WikipediaLanguage.getDefault(), 123);
        when(reviewTypeFinder.findPageReview(pageKey, options)).thenReturn(Optional.of(review));

        mvc
            .perform(
                get("/api/review/123?kind=5&subtype=Fechas&user=A")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        verify(reviewTypeFinder).findPageReview(pageKey, options);
    }

    @Test
    void testFindPageReviewByIdAndCustomReplacement() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "A");
        CustomType customType = CustomType.of("X", true, "Y");
        ReviewOptions options = ReviewOptions.ofCustom(userId, customType);
        PageKey pageKey = PageKey.of(WikipediaLanguage.getDefault(), 123);
        when(reviewCustomFinder.findPageReview(pageKey, options)).thenReturn(Optional.of(review));

        mvc
            .perform(
                get("/api/review/123?kind=1&subtype=X&cs=true&suggestion=Y&user=A")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        verify(reviewCustomFinder).findPageReview(pageKey, options);
    }
}
