package es.bvalero.replacer.replacement;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.bvalero.replacer.WebMvcConfiguration;
import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.user.*;
import java.time.LocalDate;
import java.util.*;
import javax.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ReplacementCountController.class)
@Import({ AopAutoConfiguration.class, ValidateUserAspect.class, WebMvcConfiguration.class })
class ReplacementCountControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ReplacementCountService replacementCountService;

    @Test
    void testCountReplacementsToReview() throws Exception {
        int count = new Random().nextInt();
        when(replacementCountService.countReplacementsNotReviewed(WikipediaLanguage.getDefault())).thenReturn(count);

        mvc
            .perform(
                get("/api/replacement/count?reviewed=false")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count", is(count)));

        verify(replacementCountService).countReplacementsNotReviewed(WikipediaLanguage.getDefault());
    }

    @Test
    void testCountReplacementsReviewed() throws Exception {
        int count = new Random().nextInt();
        when(replacementCountService.countReplacementsReviewed(WikipediaLanguage.getDefault())).thenReturn(count);

        mvc
            .perform(
                get("/api/replacement/count?reviewed=true")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count", is(count)));

        verify(replacementCountService).countReplacementsReviewed(WikipediaLanguage.getDefault());
    }

    @Test
    void testCountReplacementsGroupedByReviewer() throws Exception {
        ResultCount<String> count = ResultCount.of("X", 100);
        when(replacementCountService.countReplacementsGroupedByReviewer(WikipediaLanguage.getDefault()))
            .thenReturn(Collections.singletonList(count));

        mvc
            .perform(
                get("/api/replacement/user/count")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].reviewer", is("X")))
            .andExpect(jsonPath("$[0].count", is(100)));

        verify(replacementCountService).countReplacementsGroupedByReviewer(WikipediaLanguage.getDefault());
    }

    @Test
    void testCountPagesWithMoreReplacementsToReview() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "x");
        AccessToken accessToken = AccessToken.of("a", "b");
        User user = User.builder().id(userId).accessToken(accessToken).admin(true).build();
        when(userService.findAuthenticatedUser(WikipediaLanguage.getDefault(), accessToken))
            .thenReturn(Optional.of(user));

        IndexedPage page = IndexedPage
            .builder()
            .pageKey(PageKey.of(WikipediaLanguage.getDefault(), 2))
            .title("T")
            .lastUpdate(LocalDate.now())
            .build();
        Collection<ResultCount<IndexedPage>> counts = List.of(ResultCount.of(page, 100));

        when(replacementCountService.countNotReviewedGroupedByPage(WikipediaLanguage.getDefault())).thenReturn(counts);

        mvc
            .perform(
                get("/api/replacement/page/count")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(new Cookie(AccessToken.COOKIE_NAME, accessToken.toCookieValue()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].pageId", is(page.getPageKey().getPageId())))
            .andExpect(jsonPath("$[0].title", is(page.getTitle())))
            .andExpect(jsonPath("$[0].count", is(100)));

        verify(replacementCountService).countNotReviewedGroupedByPage(WikipediaLanguage.getDefault());
    }

    @Test
    void testCountPagesWithMoreReplacementsToReviewNotAdmin() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "x");
        AccessToken accessToken = AccessToken.of("a", "b");
        User user = User.builder().id(userId).accessToken(accessToken).admin(false).build();
        when(userService.findAuthenticatedUser(WikipediaLanguage.getDefault(), accessToken))
            .thenReturn(Optional.of(user));

        mvc
            .perform(
                get("/api/replacement/page/count")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(new Cookie(AccessToken.COOKIE_NAME, accessToken.toCookieValue()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());

        verify(replacementCountService, never()).countNotReviewedGroupedByPage(WikipediaLanguage.getDefault());
    }
}
