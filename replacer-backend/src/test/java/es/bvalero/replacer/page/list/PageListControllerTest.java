package es.bvalero.replacer.page.list;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ForbiddenException;
import es.bvalero.replacer.user.UserRightsService;
import es.bvalero.replacer.user.ValidateUserAspect;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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
    private PageReviewByTypeService pageReviewByTypeService;

    @Test
    void testFindPagesToReviewByType() throws Exception {
        mvc
            .perform(get("/api/page/type?lang=es&user=A&kind=2&subtype=Africa").contentType(MediaType.TEXT_PLAIN_VALUE))
            .andExpect(status().isOk());

        verify(userRightsService).validateBotUser(WikipediaLanguage.SPANISH, "A");
        verify(pageFindByTypeService)
            .findPagesToReviewByType(WikipediaLanguage.SPANISH, ReplacementType.of(ReplacementKind.SIMPLE, "Africa"));
    }

    @Test
    void testFindPagesToReviewByTypeNotBot() throws Exception {
        doThrow(ForbiddenException.class)
            .when(userRightsService)
            .validateBotUser(any(WikipediaLanguage.class), anyString());

        mvc
            .perform(get("/api/page/type?lang=es&user=A&kind=2&subtype=Africa").contentType(MediaType.TEXT_PLAIN_VALUE))
            .andExpect(status().isForbidden());

        verify(userRightsService).validateBotUser(WikipediaLanguage.SPANISH, "A");
        verify(pageFindByTypeService, never())
            .findPagesToReviewByType(WikipediaLanguage.SPANISH, ReplacementType.of(ReplacementKind.SIMPLE, "Africa"));
    }

    @Test
    void testReviewPagesByType() throws Exception {
        mvc
            .perform(
                post("/api/page/type/review?kind=2&subtype=Africa&lang=es&user=A").contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());

        verify(userRightsService).validateBotUser(WikipediaLanguage.SPANISH, "A");
        verify(pageReviewByTypeService)
            .reviewPagesByType(WikipediaLanguage.SPANISH, ReplacementType.of(ReplacementKind.SIMPLE, "Africa"));
    }

    @Test
    void testReviewPagesByTypeNotBot() throws Exception {
        doThrow(ForbiddenException.class)
            .when(userRightsService)
            .validateBotUser(any(WikipediaLanguage.class), anyString());

        mvc
            .perform(
                post("/api/page/type/review?kind=2&subtype=Africa&lang=es&user=A").contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());

        verify(userRightsService).validateBotUser(WikipediaLanguage.SPANISH, "A");
        verify(pageReviewByTypeService, never())
            .reviewPagesByType(WikipediaLanguage.SPANISH, ReplacementType.of(ReplacementKind.SIMPLE, "Africa"));
    }
}
