package es.bvalero.replacer.page.save;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.WebMvcConfiguration;
import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.ReplacementKind;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.user.User;
import es.bvalero.replacer.user.util.WebUtils;
import es.bvalero.replacer.wikipedia.WikipediaConflictException;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = { PageSaveController.class, WebMvcConfiguration.class })
class PageSaveControllerTest {

    private static final int pageId = 123;
    private static final PageKey pageKey = PageKey.of(WikipediaLanguage.getDefault(), pageId);
    private static final ReviewedReplacement reviewedReplacement = ReviewedReplacement.builder()
        .pageKey(pageKey)
        .type(StandardType.of(ReplacementKind.SIMPLE, "1"))
        .start(1)
        .reviewer("x")
        .fixed(true)
        .build();
    private static final ReviewedPage reviewedPage = ReviewedPage.builder()
        .pageKey(pageKey)
        .title("T")
        .content("X")
        .queryTimestamp(WikipediaTimestamp.now())
        .reviewedReplacements(List.of(reviewedReplacement))
        .build();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebUtils webUtils;

    @MockitoBean
    private PageSaveApi pageSaveApi;

    private ReviewedReplacementDto reviewedReplacementDto;
    private ReviewedPageDto reviewedPageWithChanges;

    @BeforeEach
    public void setUp() {
        reviewedReplacementDto = new ReviewedReplacementDto();
        reviewedReplacementDto.setKind(reviewedReplacement.getType().getKind().getCode());
        reviewedReplacementDto.setSubtype(reviewedReplacement.getType().getSubtype());
        reviewedReplacementDto.setStart(reviewedReplacement.getStart());
        reviewedReplacementDto.setFixed(reviewedReplacement.isFixed());

        reviewedPageWithChanges = new ReviewedPageDto();
        reviewedPageWithChanges.setTitle(reviewedPage.getTitle());
        reviewedPageWithChanges.setContent(reviewedPage.getContent());
        reviewedPageWithChanges.setQueryTimestamp(Objects.requireNonNull(reviewedPage.getQueryTimestamp()).toString());
        reviewedPageWithChanges.setReviewedReplacements(List.of(reviewedReplacementDto));
    }

    @Test
    void testSaveWithChanges() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        mvc
            .perform(
                post("/api/page/123")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewedPageWithChanges))
            )
            .andExpect(status().isNoContent());

        verify(pageSaveApi).save(reviewedPage, user);
    }

    @Test
    void testSaveWithNoChanges() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        ReviewedPageDto reviewedPageWithoutChanges = new ReviewedPageDto();
        reviewedPageWithoutChanges.setReviewedReplacements(List.of(reviewedReplacementDto));
        reviewedPageWithoutChanges.getReviewedReplacements().forEach(rr -> rr.setFixed(false));

        mvc
            .perform(
                post("/api/page/123")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewedPageWithoutChanges))
            )
            .andExpect(status().isNoContent());

        ReviewedPage reviewedPage = ReviewedPage.builder()
            .pageKey(pageKey)
            .reviewedReplacements(List.of(reviewedReplacement.withFixed(false)))
            .build();

        verify(pageSaveApi).save(reviewedPage, user);
    }

    @Test
    void testPageNotValidEmptyContent() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        ReviewedPageDto reviewedPageWithoutChanges = new ReviewedPageDto();
        reviewedPageWithoutChanges.setContent("");

        mvc
            .perform(
                post("/api/page/123")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewedPageWithoutChanges))
            )
            .andExpect(status().isBadRequest());

        verify(pageSaveApi, never()).save(any(ReviewedPage.class), any(User.class));
    }

    @Test
    void testSaveWithChangesWithConflict() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        doThrow(WikipediaConflictException.class).when(pageSaveApi).save(reviewedPage, user);

        mvc
            .perform(
                post("/api/page/123")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewedPageWithChanges))
            )
            .andExpect(status().isConflict());

        verify(pageSaveApi).save(reviewedPage, user);
    }

    @Test
    void testSaveWithChangesNotAuthorizedWikipedia() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        doThrow(new WikipediaException("mwoauth-invalid-authorization")).when(pageSaveApi).save(reviewedPage, user);

        mvc
            .perform(
                post("/api/page/123")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewedPageWithChanges))
            )
            .andExpect(status().isUnauthorized());

        verify(pageSaveApi).save(reviewedPage, user);
    }

    @Test
    void testSaveWithChangesWikipediaException() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        doThrow(WikipediaException.class).when(pageSaveApi).save(reviewedPage, user);

        mvc
            .perform(
                post("/api/page/123")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewedPageWithChanges))
            )
            .andExpect(status().isInternalServerError());

        verify(pageSaveApi).save(reviewedPage, user);
    }
}
