package es.bvalero.replacer.replacement;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.bvalero.replacer.WebMvcConfiguration;
import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.util.WebUtils;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.user.AccessToken;
import es.bvalero.replacer.user.User;
import es.bvalero.replacer.user.ValidateUserAspect;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
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
    private WebUtils webUtils;

    @MockBean
    private ReplacementCountService replacementCountService;

    @Test
    void testCountReplacementsToReview() throws Exception {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        when(webUtils.getLanguageHeader(any(HttpServletRequest.class))).thenReturn(lang);

        int count = new Random().nextInt();
        when(replacementCountService.countNotReviewed(WikipediaLanguage.getDefault())).thenReturn(count);

        mvc
            .perform(
                get("/api/replacement/count?reviewed=false")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count", is(count)));

        verify(replacementCountService).countNotReviewed(WikipediaLanguage.getDefault());
    }

    @Test
    void testCountReplacementsReviewed() throws Exception {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        when(webUtils.getLanguageHeader(any(HttpServletRequest.class))).thenReturn(lang);

        int count = new Random().nextInt();
        when(replacementCountService.countReviewed(WikipediaLanguage.getDefault())).thenReturn(count);

        mvc
            .perform(
                get("/api/replacement/count?reviewed=true")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count", is(count)));

        verify(replacementCountService).countReviewed(WikipediaLanguage.getDefault());
    }

    @Test
    void testCountReplacementsGroupedByReviewer() throws Exception {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        when(webUtils.getLanguageHeader(any(HttpServletRequest.class))).thenReturn(lang);

        ResultCount<String> count = ResultCount.of("X", 100);
        when(replacementCountService.countReviewedGroupedByReviewer(WikipediaLanguage.getDefault()))
            .thenReturn(List.of(count));

        mvc
            .perform(
                get("/api/replacement/user/count")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].reviewer", is("X")))
            .andExpect(jsonPath("$[0].count", is(100)));

        verify(replacementCountService).countReviewedGroupedByReviewer(WikipediaLanguage.getDefault());
    }

    @Test
    void testCountPagesWithMoreReplacementsToReview() throws Exception {
        User user = User.buildTestAdminUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        when(webUtils.getLanguageHeader(any(HttpServletRequest.class))).thenReturn(lang);

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
                    .cookie(new Cookie(AccessToken.COOKIE_NAME, user.getAccessToken().toCookieValue()))
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
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        mvc
            .perform(
                get("/api/replacement/page/count")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(new Cookie(AccessToken.COOKIE_NAME, user.getAccessToken().toCookieValue()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());

        verify(replacementCountService, never()).countNotReviewedGroupedByPage(WikipediaLanguage.getDefault());
    }
}
