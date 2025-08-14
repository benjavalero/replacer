package es.bvalero.replacer.replacement.count;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.bvalero.replacer.WebMvcConfiguration;
import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.User;
import es.bvalero.replacer.user.ValidateUserAspect;
import es.bvalero.replacer.user.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ReplacementCountController.class)
@Import({ AopAutoConfiguration.class, ValidateUserAspect.class, WebMvcConfiguration.class })
class ReplacementCountControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private WebUtils webUtils;

    @MockitoBean
    private ReplacementCountApi replacementCountApi;

    @Test
    void testCountReplacementsToReview() throws Exception {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        when(webUtils.getLanguageHeader(any(HttpServletRequest.class))).thenReturn(lang);

        int count = new Random().nextInt();
        when(replacementCountApi.countNotReviewed(WikipediaLanguage.getDefault())).thenReturn(count);

        mvc
            .perform(
                get("/api/replacement/count?reviewed=false")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count", is(count)));

        verify(replacementCountApi).countNotReviewed(WikipediaLanguage.getDefault());
    }

    @Test
    void testCountReplacementsReviewed() throws Exception {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        when(webUtils.getLanguageHeader(any(HttpServletRequest.class))).thenReturn(lang);

        int count = new Random().nextInt();
        when(replacementCountApi.countReviewed(WikipediaLanguage.getDefault())).thenReturn(count);

        mvc
            .perform(
                get("/api/replacement/count?reviewed=true")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count", is(count)));

        verify(replacementCountApi).countReviewed(WikipediaLanguage.getDefault());
    }

    @Test
    void testCountReplacementsGroupedByReviewer() throws Exception {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        when(webUtils.getLanguageHeader(any(HttpServletRequest.class))).thenReturn(lang);

        ResultCount<String> count = ResultCount.of("X", 100);
        when(replacementCountApi.countReviewedGroupedByReviewer(WikipediaLanguage.getDefault())).thenReturn(
            List.of(count)
        );

        mvc
            .perform(
                get("/api/replacement/user/count")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].reviewer", is("X")))
            .andExpect(jsonPath("$[0].count", is(100)));

        verify(replacementCountApi).countReviewedGroupedByReviewer(WikipediaLanguage.getDefault());
    }

    @Test
    void testCountPagesWithMoreReplacementsToReview() throws Exception {
        User user = User.buildTestAdminUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        when(webUtils.getLanguageHeader(any(HttpServletRequest.class))).thenReturn(lang);

        PageTitle page = PageTitle.of(PageKey.of(WikipediaLanguage.getDefault(), 2), "T");
        Collection<ResultCount<PageTitle>> counts = List.of(ResultCount.of(page, 100));

        when(replacementCountApi.countNotReviewedGroupedByPage(WikipediaLanguage.getDefault())).thenReturn(counts);

        mvc
            .perform(
                get("/api/replacement/page/count")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].pageId", is(page.getPageKey().getPageId())))
            .andExpect(jsonPath("$[0].title", is(page.getTitle())))
            .andExpect(jsonPath("$[0].count", is(100)));

        verify(replacementCountApi).countNotReviewedGroupedByPage(WikipediaLanguage.getDefault());
    }

    @Test
    void testCountPagesWithMoreReplacementsToReviewNotAdmin() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        mvc
            .perform(
                get("/api/replacement/page/count")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());

        verify(replacementCountApi, never()).countNotReviewedGroupedByPage(WikipediaLanguage.getDefault());
    }
}
