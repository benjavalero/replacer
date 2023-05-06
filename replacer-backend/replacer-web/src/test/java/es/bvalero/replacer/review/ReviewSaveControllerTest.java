package es.bvalero.replacer.review;

import static es.bvalero.replacer.review.ReviewPage.EMPTY_CONTENT;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.WebMvcConfiguration;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.user.*;
import es.bvalero.replacer.wikipedia.*;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.Cookie;
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
    private static final String title = "Q";
    private static final String content = "X";
    private static final WikipediaTimestamp timestamp = WikipediaTimestamp.now();
    private static final WikipediaPage page = WikipediaPage
        .builder()
        .pageKey(PageKey.of(WikipediaLanguage.getDefault(), pageId))
        .namespace(WikipediaNamespace.getDefault())
        .title(title)
        .content(content)
        .lastUpdate(timestamp)
        .queryTimestamp(timestamp)
        .build();
    ReviewedReplacement reviewed = ReviewedReplacement
        .builder()
        .pageKey(page.getPageKey())
        .type(StandardType.of(ReplacementKind.SIMPLE, "1"))
        .start(1)
        .reviewer("x")
        .build();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private ReviewSaveService reviewSaveService;

    private SaveReviewRequest request;

    @BeforeEach
    public void setUp() {
        this.request = new SaveReviewRequest();
        ReviewPage reviewPage = new ReviewPage();
        reviewPage.setLang(WikipediaLanguage.getDefault().getCode());
        reviewPage.setPageId(pageId);
        reviewPage.setTitle(title);
        reviewPage.setContent(content);
        reviewPage.setQueryTimestamp(timestamp.toString());
        request.setPage(reviewPage);
        ReviewedReplacementDto reviewedDto = new ReviewedReplacementDto();
        reviewedDto.setKind(reviewed.getType().getKind().getCode());
        reviewedDto.setSubtype(reviewed.getType().getSubtype());
        reviewedDto.setStart(reviewed.getStart());
        request.setReviewedReplacements(List.of(reviewedDto));
    }

    @Test
    void testSaveWithChanges() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "x");
        AccessToken accessToken = AccessToken.of("a", "b");
        User user = User.builder().id(userId).accessToken(accessToken).build();
        when(userService.findAuthenticatedUser(WikipediaLanguage.getDefault(), accessToken))
            .thenReturn(Optional.of(user));

        mvc
            .perform(
                post("/api/review/123")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(new Cookie(AccessToken.COOKIE_NAME, accessToken.toCookieValue()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isNoContent());

        verify(reviewSaveService).saveReviewContent(page, null, List.of(reviewed), accessToken);
        verify(reviewSaveService).markAsReviewed(List.of(reviewed), true);
    }

    @Test
    void testSaveWithNoChanges() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "x");
        AccessToken accessToken = AccessToken.of("a", "b");
        User user = User.builder().id(userId).accessToken(accessToken).build();
        when(userService.findAuthenticatedUser(WikipediaLanguage.getDefault(), accessToken))
            .thenReturn(Optional.of(user));

        request.getPage().setContent(EMPTY_CONTENT);

        mvc
            .perform(
                post("/api/review/123")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(new Cookie(AccessToken.COOKIE_NAME, accessToken.toCookieValue()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isNoContent());

        verify(reviewSaveService, never())
            .saveReviewContent(any(WikipediaPage.class), anyInt(), anyCollection(), any(AccessToken.class));
        verify(reviewSaveService).markAsReviewed(List.of(reviewed), false);
    }

    @Test
    void testPageIdMismatch() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "x");
        AccessToken accessToken = AccessToken.of("a", "b");
        User user = User.builder().id(userId).accessToken(accessToken).build();
        when(userService.findAuthenticatedUser(WikipediaLanguage.getDefault(), accessToken))
            .thenReturn(Optional.of(user));

        mvc
            .perform(
                post("/api/review/321")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(new Cookie(AccessToken.COOKIE_NAME, accessToken.toCookieValue()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());

        verify(reviewSaveService, never()).markAsReviewed(anyCollection(), anyBoolean());
    }

    @Test
    void testPageLanguageMismatch() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "x");
        AccessToken accessToken = AccessToken.of("a", "b");
        User user = User.builder().id(userId).accessToken(accessToken).build();
        when(userService.findAuthenticatedUser(WikipediaLanguage.getDefault(), accessToken))
            .thenReturn(Optional.of(user));

        mvc
            .perform(
                post("/api/review/123")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, "en")
                    .cookie(new Cookie(AccessToken.COOKIE_NAME, accessToken.toCookieValue()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());

        verify(reviewSaveService, never()).markAsReviewed(anyCollection(), anyBoolean());
    }

    @Test
    void testPageNotValidEmptyContent() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "x");
        AccessToken accessToken = AccessToken.of("a", "b");
        User user = User.builder().id(userId).accessToken(accessToken).build();
        when(userService.findAuthenticatedUser(WikipediaLanguage.getDefault(), accessToken))
            .thenReturn(Optional.of(user));

        request.getPage().setContent("");

        mvc
            .perform(
                post("/api/review/123")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(new Cookie(AccessToken.COOKIE_NAME, accessToken.toCookieValue()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());

        verify(reviewSaveService, never()).markAsReviewed(anyCollection(), anyBoolean());
    }

    @Test
    void testSaveWithChangesWithConflict() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "x");
        AccessToken accessToken = AccessToken.of("a", "b");
        User user = User.builder().id(userId).accessToken(accessToken).build();
        when(userService.findAuthenticatedUser(WikipediaLanguage.getDefault(), accessToken))
            .thenReturn(Optional.of(user));

        doThrow(WikipediaConflictException.class)
            .when(reviewSaveService)
            .saveReviewContent(page, null, List.of(reviewed), accessToken);

        mvc
            .perform(
                post("/api/review/123")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(new Cookie(AccessToken.COOKIE_NAME, accessToken.toCookieValue()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isConflict());

        verify(reviewSaveService).saveReviewContent(page, null, List.of(reviewed), accessToken);
        verify(reviewSaveService, never()).markAsReviewed(anyCollection(), anyBoolean());
    }

    @Test
    void testSaveWithChangesNotAuthorizedWikipedia() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "x");
        AccessToken accessToken = AccessToken.of("a", "b");
        User user = User.builder().id(userId).accessToken(accessToken).build();
        when(userService.findAuthenticatedUser(WikipediaLanguage.getDefault(), accessToken))
            .thenReturn(Optional.of(user));

        doThrow(new WikipediaException("mwoauth-invalid-authorization"))
            .when(reviewSaveService)
            .saveReviewContent(page, null, List.of(reviewed), accessToken);

        mvc
            .perform(
                post("/api/review/123")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(new Cookie(AccessToken.COOKIE_NAME, accessToken.toCookieValue()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isUnauthorized());

        verify(reviewSaveService).saveReviewContent(page, null, List.of(reviewed), accessToken);
        verify(reviewSaveService, never()).markAsReviewed(anyCollection(), anyBoolean());
    }

    @Test
    void testSaveWithChangesWikipediaException() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "x");
        AccessToken accessToken = AccessToken.of("a", "b");
        User user = User.builder().id(userId).accessToken(accessToken).build();
        when(userService.findAuthenticatedUser(WikipediaLanguage.getDefault(), accessToken))
            .thenReturn(Optional.of(user));

        doThrow(WikipediaException.class)
            .when(reviewSaveService)
            .saveReviewContent(page, null, List.of(reviewed), accessToken);

        mvc
            .perform(
                post("/api/review/123")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(new Cookie(AccessToken.COOKIE_NAME, accessToken.toCookieValue()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isInternalServerError());

        verify(reviewSaveService).saveReviewContent(page, null, List.of(reviewed), accessToken);
        verify(reviewSaveService, never()).markAsReviewed(anyCollection(), anyBoolean());
    }
}
