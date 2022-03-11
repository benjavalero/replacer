package es.bvalero.replacer.page.list;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ForbiddenException;
import es.bvalero.replacer.user.UserRightsService;
import es.bvalero.replacer.user.ValidateUserAspect;
import java.util.Collections;
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
    private PageUnreviewedTitleListService pageUnreviewedTitleListService;

    @MockBean
    private ReviewByTypeService reviewByTypeService;

    @MockBean
    private PageMostUnreviewedService pageMostUnreviewedService;

    @Test
    void testFindPageTitlesToReviewByType() throws Exception {
        mvc
            .perform(get("/api/pages?type=2&subtype=Africa&lang=es&user=A").contentType(MediaType.TEXT_PLAIN_VALUE))
            .andExpect(status().isOk());

        verify(userRightsService).validateBotUser(WikipediaLanguage.SPANISH, "A");
        verify(pageUnreviewedTitleListService)
            .findPageTitlesToReviewByType(
                WikipediaLanguage.SPANISH,
                ReplacementType.of(ReplacementKind.SIMPLE, "Africa")
            );
    }

    @Test
    void testFindPageTitlesToReviewByTypeNotBot() throws Exception {
        doThrow(ForbiddenException.class)
            .when(userRightsService)
            .validateBotUser(any(WikipediaLanguage.class), anyString());

        mvc
            .perform(get("/api/pages?type=2&subtype=Africa&lang=es&user=A").contentType(MediaType.TEXT_PLAIN_VALUE))
            .andExpect(status().isForbidden());

        verify(userRightsService).validateBotUser(WikipediaLanguage.SPANISH, "A");
        verify(pageUnreviewedTitleListService, never())
            .findPageTitlesToReviewByType(
                WikipediaLanguage.SPANISH,
                ReplacementType.of(ReplacementKind.SIMPLE, "Africa")
            );
    }

    @Test
    void testReviewAsSystemByType() throws Exception {
        mvc
            .perform(
                post("/api/pages/review?type=2&subtype=Africa&lang=es&user=A").contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());

        verify(userRightsService).validateBotUser(WikipediaLanguage.SPANISH, "A");
        verify(reviewByTypeService)
            .reviewAsSystemByType(WikipediaLanguage.SPANISH, ReplacementType.of(ReplacementKind.SIMPLE, "Africa"));
    }

    @Test
    void testReviewAsSystemByTypeNotBot() throws Exception {
        doThrow(ForbiddenException.class)
            .when(userRightsService)
            .validateBotUser(any(WikipediaLanguage.class), anyString());

        mvc
            .perform(
                post("/api/pages/review?type=2&subtype=Africa&lang=es&user=A").contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());

        verify(userRightsService).validateBotUser(WikipediaLanguage.SPANISH, "A");
        verify(reviewByTypeService, never())
            .reviewAsSystemByType(WikipediaLanguage.SPANISH, ReplacementType.of(ReplacementKind.SIMPLE, "Africa"));
    }

    @Test
    void testCountPagesWithMoreReplacementsToReview() throws Exception {
        PageCount count = PageCount.of(3, "X", 100);
        when(pageMostUnreviewedService.countPagesWithMoreReplacementsToReview(WikipediaLanguage.SPANISH))
            .thenReturn(Collections.singletonList(count));

        mvc
            .perform(get("/api/pages/unreviewed?lang=es&user=A").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].pageId", is(3)))
            .andExpect(jsonPath("$[0].title", is("X")))
            .andExpect(jsonPath("$[0].count", is(100)));

        verify(userRightsService).validateAdminUser(WikipediaLanguage.SPANISH, "A");
        verify(pageMostUnreviewedService).countPagesWithMoreReplacementsToReview(WikipediaLanguage.SPANISH);
    }

    @Test
    void testCountPagesWithMoreReplacementsToReviewNotAdmin() throws Exception {
        doThrow(ForbiddenException.class)
            .when(userRightsService)
            .validateAdminUser(any(WikipediaLanguage.class), anyString());

        mvc
            .perform(get("/api/pages/unreviewed?lang=es&user=A").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        verify(userRightsService).validateAdminUser(WikipediaLanguage.SPANISH, "A");
        verify(pageMostUnreviewedService, never()).countPagesWithMoreReplacementsToReview(WikipediaLanguage.SPANISH);
    }
}
