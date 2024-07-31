package es.bvalero.replacer.page.save;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.WebMvcConfiguration;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.util.WebUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.find.WikipediaTimestamp;
import es.bvalero.replacer.user.User;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
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
@WebMvcTest(controllers = { ReviewSaveController.class, WebMvcConfiguration.class })
class ReviewSaveControllerTest {

    private static final int pageId = 123;
    private static final PageKey pageKey = PageKey.of(WikipediaLanguage.getDefault(), pageId);
    private static final String title = "T";
    private static final String content = "X";
    private static final WikipediaTimestamp timestamp = WikipediaTimestamp.now();
    private static final ReviewedReplacement reviewedReplacement = ReviewedReplacement.builder()
        .pageKey(pageKey)
        .type(StandardType.of(ReplacementKind.SIMPLE, "1"))
        .start(1)
        .reviewer("x")
        .fixed(true)
        .build();
    private static final WikipediaPageSaveCommand pageSave = WikipediaPageSaveCommand.builder()
        .pageKey(pageKey)
        .content(content)
        .editSummary("S")
        .queryTimestamp(timestamp)
        .build();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebUtils webUtils;

    @MockBean
    private ApplyCosmeticsService applyCosmeticsService;

    @MockBean
    private ReviewSaveService reviewSaveService;

    private ReviewedPage reviewedPageWithoutChanges;
    private ReviewedPage reviewedPageWithChanges;

    @BeforeEach
    public void setUp() {
        ReviewedReplacementDto reviewedDto = new ReviewedReplacementDto();
        reviewedDto.setKind(reviewedReplacement.getType().getKind().getCode());
        reviewedDto.setSubtype(reviewedReplacement.getType().getSubtype());
        reviewedDto.setStart(reviewedReplacement.getStart());
        reviewedDto.setFixed(reviewedReplacement.isFixed());

        reviewedPageWithoutChanges = new ReviewedPage();
        reviewedPageWithoutChanges.setReviewedReplacements(List.of(reviewedDto));

        reviewedPageWithChanges = new ReviewedPage();
        reviewedPageWithChanges.setTitle(title);
        reviewedPageWithChanges.setContent(content);
        reviewedPageWithChanges.setQueryTimestamp(timestamp.toString());
        reviewedPageWithChanges.setReviewedReplacements(List.of(reviewedDto));
    }

    @Test
    void testSaveWithChanges() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        when(applyCosmeticsService.applyCosmeticChanges(any(FinderPage.class))).thenReturn(pageSave.getContent());
        when(reviewSaveService.buildEditSummary(anyCollection(), anyBoolean())).thenReturn(pageSave.getEditSummary());
        WikipediaPageSaveResult pageSaveResult = WikipediaPageSaveResult.ofDummy();
        when(reviewSaveService.saveReviewContent(pageSave, user)).thenReturn(pageSaveResult);

        mvc
            .perform(
                post("/api/page/123")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewedPageWithChanges))
            )
            .andExpect(status().isNoContent());

        verify(reviewSaveService).saveReviewContent(pageSave, user);
        verify(reviewSaveService).markAsReviewed(List.of(reviewedReplacement), pageSaveResult);
    }

    @Test
    void testSaveWithNoChanges() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        mvc
            .perform(
                post("/api/page/123")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewedPageWithoutChanges))
            )
            .andExpect(status().isNoContent());

        verify(reviewSaveService, never()).saveReviewContent(any(WikipediaPageSaveCommand.class), any(User.class));
        verify(reviewSaveService).markAsReviewed(List.of(reviewedReplacement), null);
    }

    @Test
    void testPageNotValidEmptyContent() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

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

        verify(reviewSaveService, never()).markAsReviewed(anyCollection(), any(WikipediaPageSaveResult.class));
    }

    @Test
    void testSaveWithChangesWithConflict() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        when(applyCosmeticsService.applyCosmeticChanges(any(FinderPage.class))).thenReturn(pageSave.getContent());
        when(reviewSaveService.buildEditSummary(anyCollection(), anyBoolean())).thenReturn(pageSave.getEditSummary());

        doThrow(WikipediaConflictException.class).when(reviewSaveService).saveReviewContent(pageSave, user);

        mvc
            .perform(
                post("/api/page/123")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewedPageWithChanges))
            )
            .andExpect(status().isConflict());

        verify(reviewSaveService).saveReviewContent(pageSave, user);
        verify(reviewSaveService, never()).markAsReviewed(anyCollection(), any(WikipediaPageSaveResult.class));
    }

    @Test
    void testSaveWithChangesNotAuthorizedWikipedia() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        when(applyCosmeticsService.applyCosmeticChanges(any(FinderPage.class))).thenReturn(pageSave.getContent());
        when(reviewSaveService.buildEditSummary(anyCollection(), anyBoolean())).thenReturn(pageSave.getEditSummary());

        doThrow(new WikipediaException("mwoauth-invalid-authorization"))
            .when(reviewSaveService)
            .saveReviewContent(pageSave, user);

        mvc
            .perform(
                post("/api/page/123")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewedPageWithChanges))
            )
            .andExpect(status().isUnauthorized());

        verify(reviewSaveService).saveReviewContent(pageSave, user);
        verify(reviewSaveService, never()).markAsReviewed(anyCollection(), any(WikipediaPageSaveResult.class));
    }

    @Test
    void testSaveWithChangesWikipediaException() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        when(applyCosmeticsService.applyCosmeticChanges(any(FinderPage.class))).thenReturn(pageSave.getContent());
        when(reviewSaveService.buildEditSummary(anyCollection(), anyBoolean())).thenReturn(pageSave.getEditSummary());

        doThrow(WikipediaException.class).when(reviewSaveService).saveReviewContent(pageSave, user);

        mvc
            .perform(
                post("/api/page/123")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reviewedPageWithChanges))
            )
            .andExpect(status().isInternalServerError());

        verify(reviewSaveService).saveReviewContent(pageSave, user);
        verify(reviewSaveService, never()).markAsReviewed(anyCollection(), any(WikipediaPageSaveResult.class));
    }
}
