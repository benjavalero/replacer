package es.bvalero.replacer.page.list;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.bvalero.replacer.WebMvcConfiguration;
import es.bvalero.replacer.common.domain.User;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.ReplacementKind;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.user.ValidateUserAspect;
import es.bvalero.replacer.user.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
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
@WebMvcTest(controllers = PageListController.class)
@Import({ AopAutoConfiguration.class, ValidateUserAspect.class, WebMvcConfiguration.class })
class PageListControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private WebUtils webUtils;

    @MockitoBean
    private PageListApi pageListApi;

    @Test
    void testFindPagesToReviewByType() throws Exception {
        User user = User.buildTestBotUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        mvc
            .perform(get("/api/page/type?lang=es&kind=2&subtype=Africa").contentType(MediaType.TEXT_PLAIN_VALUE))
            .andExpect(status().isOk());

        verify(pageListApi).findPageTitlesNotReviewedByType(
            WikipediaLanguage.getDefault(),
            StandardType.of(ReplacementKind.SIMPLE, "Africa")
        );
    }

    @Test
    void testReviewPagesByType() throws Exception {
        User user = User.buildTestBotUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        when(webUtils.getLanguageHeader(any(HttpServletRequest.class))).thenReturn(lang);

        mvc
            .perform(
                post("/api/page/type?kind=2&subtype=Africa")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());

        verify(pageListApi).updateSystemReviewerByType(
            WikipediaLanguage.getDefault(),
            StandardType.of(ReplacementKind.SIMPLE, "Africa")
        );
    }

    @Test
    void testReviewPagesByTypeNotBot() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        mvc
            .perform(
                post("/api/page/type?kind=2&subtype=Africa")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());

        verify(pageListApi, never()).updateSystemReviewerByType(
            WikipediaLanguage.getDefault(),
            StandardType.of(ReplacementKind.SIMPLE, "Africa")
        );
    }
}
