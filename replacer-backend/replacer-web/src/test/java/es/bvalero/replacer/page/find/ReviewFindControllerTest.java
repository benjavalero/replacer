package es.bvalero.replacer.page.find;

import static es.bvalero.replacer.page.find.ReviewFindController.TOTAL_PAGES_HEADER;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.WebMvcConfiguration;
import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.util.WebUtils;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementKind;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.user.User;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = { ReviewFindController.class, WebMvcConfiguration.class })
class ReviewFindControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebUtils webUtils;

    @MockitoBean
    private ReviewNoTypeFinder reviewNoTypeFinder;

    @MockitoBean
    private ReviewTypeFinder reviewTypeFinder;

    @MockitoBean
    private ReviewCustomFinder reviewCustomFinder;

    private final int pageId = 3;
    private final String title = "T";
    private final String content = "ABCDE";
    private final WikipediaTimestamp now = WikipediaTimestamp.now();
    private final WikipediaTimestamp queryTimestamp = now;
    private final WikipediaPage page = WikipediaPage.builder()
        .pageKey(PageKey.of(WikipediaLanguage.getDefault(), pageId))
        .namespace(WikipediaNamespace.getDefault())
        .title(title)
        .content(content)
        .lastUpdate(now)
        .queryTimestamp(queryTimestamp)
        .build();
    private final int sectionId = 2;
    private final String anchor = "S";
    private final WikipediaSection section = WikipediaSection.builder()
        .pageKey(page.getPageKey())
        .index(sectionId)
        .level(2)
        .byteOffset(0)
        .anchor(anchor)
        .build();
    private final int start = 2;
    private final String rep = "C";
    private final Suggestion suggestion = Suggestion.of("c", "ç");
    private final Replacement replacement = Replacement.of(
        start,
        rep,
        StandardType.of(ReplacementKind.SIMPLE, rep),
        List.of(suggestion),
        page.getContent()
    );
    private final int numPending = 100;
    private final Review review = Review.of(page, section, List.of(replacement), numPending);

    @Test
    void testFindRandomPageWithReplacements() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        ReviewOptions options = ReviewOptions.ofNoType(user);
        when(reviewNoTypeFinder.findRandomPageReview(options)).thenReturn(Optional.of(review));

        ResultActions resultActions = mvc
            .perform(
                get("/api/page/random")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pageId", is(pageId)))
            .andExpect(jsonPath("$.title", is(title)))
            .andExpect(jsonPath("$.content", is(content)))
            .andExpect(jsonPath("$.section.id", is(sectionId)))
            .andExpect(jsonPath("$.section.title", is(anchor)))
            .andExpect(jsonPath("$.queryTimestamp", is(queryTimestamp.toString())))
            .andExpect(jsonPath("$.replacements[0].start", is(start)))
            .andExpect(jsonPath("$.replacements[0].text", is(rep)))
            .andExpect(jsonPath("$.replacements[0].suggestions[0].text", is(rep)))
            .andExpect(jsonPath("$.replacements[0].suggestions[0].comment").doesNotExist())
            .andExpect(jsonPath("$.replacements[0].suggestions[1].text", is(suggestion.getText())))
            .andExpect(jsonPath("$.replacements[0].suggestions[1].comment", is(suggestion.getComment())));

        String numPendingHeader = resultActions.andReturn().getResponse().getHeader(TOTAL_PAGES_HEADER);
        assertNotNull(numPendingHeader);
        assertEquals(numPending, Integer.valueOf(numPendingHeader));

        verify(reviewNoTypeFinder).findRandomPageReview(options);
    }

    @Test
    void testFindRandomPageByNoType() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        ReviewOptions options = ReviewOptions.ofNoType(user);
        when(reviewNoTypeFinder.findRandomPageReview(options)).thenReturn(Optional.of(review));

        mvc
            .perform(
                get("/api/page/random")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        verify(reviewNoTypeFinder).findRandomPageReview(options);
    }

    @Test
    void testFindRandomPageByNoResults() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        ReviewOptions options = ReviewOptions.ofNoType(user);
        when(reviewNoTypeFinder.findRandomPageReview(options)).thenReturn(Optional.empty());

        mvc
            .perform(
                get("/api/page/random")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());

        verify(reviewNoTypeFinder).findRandomPageReview(options);
    }

    @Test
    void testFindRandomPageByTypeAndSubtype() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        StandardType type = StandardType.DATE;
        ReviewOptions options = ReviewOptions.ofType(user, type);
        when(reviewTypeFinder.findRandomPageReview(options)).thenReturn(Optional.of(review));

        mvc
            .perform(
                get("/api/page/random?kind=5&subtype=Fechas")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        verify(reviewTypeFinder).findRandomPageReview(options);
    }

    @Test
    void testFindRandomPageByCustomReplacement() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        ReviewOptions options = ReviewOptions.ofCustom(user, "X", false, "Y");
        when(reviewCustomFinder.findRandomPageReview(options)).thenReturn(Optional.of(review));

        mvc
            .perform(
                get("/api/page/random?kind=1&subtype=X&cs=false&suggestion=Y")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        verify(reviewCustomFinder).findRandomPageReview(options);
    }

    @Test
    void testFindPageReviewByIdWithWrongOptions() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        mvc
            .perform(
                get("/api/page/123?kind=X")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());

        verify(reviewNoTypeFinder, never()).findPageReview(any(PageKey.class), any(ReviewOptions.class));
    }

    @Test
    void testFindPageReviewById() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        ReviewOptions options = ReviewOptions.ofNoType(user);
        PageKey pageKey = PageKey.of(WikipediaLanguage.getDefault(), 123);
        when(reviewNoTypeFinder.findPageReview(pageKey, options)).thenReturn(Optional.of(review));

        mvc
            .perform(
                get("/api/page/123")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        verify(reviewNoTypeFinder).findPageReview(pageKey, options);
    }

    @Test
    void testFindPageReviewByIdByTypeAndSubtype() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        StandardType type = StandardType.DATE;
        ReviewOptions options = ReviewOptions.ofType(user, type);
        PageKey pageKey = PageKey.of(WikipediaLanguage.getDefault(), 123);
        when(reviewTypeFinder.findPageReview(pageKey, options)).thenReturn(Optional.of(review));

        mvc
            .perform(
                get("/api/page/123?kind=5&subtype=Fechas")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        verify(reviewTypeFinder).findPageReview(pageKey, options);
    }

    @Test
    void testFindPageReviewByIdAndCustomReplacement() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        ReviewOptions options = ReviewOptions.ofCustom(user, "X", true, "Y");
        PageKey pageKey = PageKey.of(WikipediaLanguage.getDefault(), 123);
        when(reviewCustomFinder.findPageReview(pageKey, options)).thenReturn(Optional.of(review));

        mvc
            .perform(
                get("/api/page/123?kind=1&subtype=X&cs=true&suggestion=Y")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        verify(reviewCustomFinder).findPageReview(pageKey, options);
    }
}
