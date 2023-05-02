package es.bvalero.replacer.page;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ForbiddenException;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.user.UserId;
import es.bvalero.replacer.user.UserRightsService;
import es.bvalero.replacer.user.ValidateUserAspect;
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
@WebMvcTest(controllers = PageListController.class)
@Import({ AopAutoConfiguration.class, ValidateUserAspect.class })
class PageListControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRightsService userRightsService;

    @MockBean
    private PageFindByTypeService pageFindByTypeService;

    @MockBean
    private ReplacementService replacementService;

    @Test
    void testFindPagesToReviewByType() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "A");
        mvc
            .perform(
                get("/api/page/type?user=A&kind=2&subtype=Africa")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.TEXT_PLAIN_VALUE)
            )
            .andExpect(status().isOk());

        verify(userRightsService).validateBotUser(userId);
        verify(pageFindByTypeService)
            .findPagesToReviewByType(WikipediaLanguage.getDefault(), StandardType.of(ReplacementKind.SIMPLE, "Africa"));
    }

    @Test
    void testFindPagesToReviewByTypeNotBot() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "A");
        doThrow(ForbiddenException.class).when(userRightsService).validateBotUser(userId);

        mvc
            .perform(
                get("/api/page/type?user=A&kind=2&subtype=Africa")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.TEXT_PLAIN_VALUE)
            )
            .andExpect(status().isForbidden());

        verify(userRightsService).validateBotUser(userId);
        verify(pageFindByTypeService, never())
            .findPagesToReviewByType(WikipediaLanguage.getDefault(), StandardType.of(ReplacementKind.SIMPLE, "Africa"));
    }

    @Test
    void testReviewPagesByType() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "A");
        mvc
            .perform(
                post("/api/page/type/review?kind=2&subtype=Africa&user=A")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());

        verify(userRightsService).validateBotUser(userId);
        verify(replacementService)
            .reviewReplacementsByType(
                WikipediaLanguage.getDefault(),
                StandardType.of(ReplacementKind.SIMPLE, "Africa")
            );
    }

    @Test
    void testReviewPagesByTypeNotBot() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "A");
        doThrow(ForbiddenException.class).when(userRightsService).validateBotUser(userId);

        mvc
            .perform(
                post("/api/page/type/review?kind=2&subtype=Africa&user=A")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());

        verify(userRightsService).validateBotUser(userId);
        verify(replacementService, never())
            .reviewReplacementsByType(
                WikipediaLanguage.getDefault(),
                StandardType.of(ReplacementKind.SIMPLE, "Africa")
            );
    }
}
