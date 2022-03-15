package es.bvalero.replacer.replacement.stats;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ForbiddenException;
import es.bvalero.replacer.repository.PageModel;
import es.bvalero.replacer.repository.ResultCount;
import es.bvalero.replacer.user.UserRightsService;
import es.bvalero.replacer.user.ValidateUserAspect;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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
@WebMvcTest(controllers = ReplacementStatsController.class)
@Import({ AopAutoConfiguration.class, ValidateUserAspect.class })
class ReplacementStatsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRightsService userRightsService;

    @MockBean
    private ReplacementStatsService replacementStatsService;

    @Test
    void testCountReplacementsToReview() throws Exception {
        int count = new Random().nextInt();
        when(replacementStatsService.countReplacementsNotReviewed(WikipediaLanguage.SPANISH)).thenReturn(count);

        mvc
            .perform(
                get("/api/replacement/count?reviewed=false&lang=es&user=A").contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count", is(count)));

        verify(replacementStatsService).countReplacementsNotReviewed(WikipediaLanguage.SPANISH);
    }

    @Test
    void testCountReplacementsReviewed() throws Exception {
        int count = new Random().nextInt();
        when(replacementStatsService.countReplacementsReviewed(WikipediaLanguage.SPANISH)).thenReturn(count);

        mvc
            .perform(get("/api/replacement/count?reviewed=true&lang=es&user=A").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count", is(count)));

        verify(replacementStatsService).countReplacementsReviewed(WikipediaLanguage.SPANISH);
    }

    @Test
    void testCountReplacementsGroupedByReviewer() throws Exception {
        ResultCount<String> count = ResultCount.of("X", 100);
        when(replacementStatsService.countReplacementsGroupedByReviewer(WikipediaLanguage.SPANISH))
            .thenReturn(Collections.singletonList(count));

        mvc
            .perform(get("/api/replacement/user/count?lang=es&user=A").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].reviewer", is("X")))
            .andExpect(jsonPath("$[0].count", is(100)));

        verify(replacementStatsService).countReplacementsGroupedByReviewer(WikipediaLanguage.SPANISH);
    }

    @Test
    void testCountPagesWithMoreReplacementsToReview() throws Exception {
        PageModel page = PageModel
            .builder()
            .lang(WikipediaLanguage.SPANISH.getCode())
            .pageId(3)
            .title("X")
            .lastUpdate(LocalDate.now())
            .build();
        Collection<ResultCount<PageModel>> counts = List.of(ResultCount.of(page, 100));

        when(replacementStatsService.countReplacementsGroupedByPage(WikipediaLanguage.SPANISH)).thenReturn(counts);

        mvc
            .perform(get("/api/replacement/page/count?lang=es&user=A").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].pageId", is(page.getPageId())))
            .andExpect(jsonPath("$[0].title", is(page.getTitle())))
            .andExpect(jsonPath("$[0].count", is(100)));

        verify(userRightsService).validateAdminUser(WikipediaLanguage.SPANISH, "A");
        verify(replacementStatsService).countReplacementsGroupedByPage(WikipediaLanguage.SPANISH);
    }

    @Test
    void testCountPagesWithMoreReplacementsToReviewNotAdmin() throws Exception {
        doThrow(ForbiddenException.class)
            .when(userRightsService)
            .validateAdminUser(any(WikipediaLanguage.class), anyString());

        mvc
            .perform(get("/api/replacement/page/count?lang=es&user=A").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        verify(userRightsService).validateAdminUser(WikipediaLanguage.SPANISH, "A");
        verify(replacementStatsService, never()).countReplacementsGroupedByPage(WikipediaLanguage.SPANISH);
    }
}
