package es.bvalero.replacer.replacement;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ForbiddenException;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
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
@WebMvcTest(controllers = ReplacementCountController.class)
@Import({ AopAutoConfiguration.class, ValidateUserAspect.class })
class ReplacementCountControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRightsService userRightsService;

    @MockBean
    private ReplacementCountService replacementCountService;

    @Test
    void testCountReplacementsToReview() throws Exception {
        int count = new Random().nextInt();
        when(replacementCountService.countReplacementsNotReviewed(WikipediaLanguage.SPANISH)).thenReturn(count);

        mvc
            .perform(
                get("/api/replacement/count?reviewed=false&lang=es&user=A").contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count", is(count)));

        verify(replacementCountService).countReplacementsNotReviewed(WikipediaLanguage.SPANISH);
    }

    @Test
    void testCountReplacementsReviewed() throws Exception {
        int count = new Random().nextInt();
        when(replacementCountService.countReplacementsReviewed(WikipediaLanguage.SPANISH)).thenReturn(count);

        mvc
            .perform(get("/api/replacement/count?reviewed=true&lang=es&user=A").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count", is(count)));

        verify(replacementCountService).countReplacementsReviewed(WikipediaLanguage.SPANISH);
    }

    @Test
    void testCountReplacementsGroupedByReviewer() throws Exception {
        ResultCount<String> count = ResultCount.of("X", 100);
        when(replacementCountService.countReplacementsGroupedByReviewer(WikipediaLanguage.SPANISH))
            .thenReturn(Collections.singletonList(count));

        mvc
            .perform(get("/api/replacement/user/count?lang=es&user=A").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].reviewer", is("X")))
            .andExpect(jsonPath("$[0].count", is(100)));

        verify(replacementCountService).countReplacementsGroupedByReviewer(WikipediaLanguage.SPANISH);
    }

    @Test
    void testCountPagesWithMoreReplacementsToReview() throws Exception {
        IndexedPage page = IndexedPage
            .builder()
            .pageKey(PageKey.of(WikipediaLanguage.SPANISH, 2))
            .title("T")
            .lastUpdate(LocalDate.now())
            .build();
        Collection<ResultCount<IndexedPage>> counts = List.of(ResultCount.of(page, 100));

        when(replacementCountService.countNotReviewedGroupedByPage(WikipediaLanguage.SPANISH)).thenReturn(counts);

        mvc
            .perform(get("/api/replacement/page/count?lang=es&user=A").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].pageId", is(page.getPageKey().getPageId())))
            .andExpect(jsonPath("$[0].title", is(page.getTitle())))
            .andExpect(jsonPath("$[0].count", is(100)));

        verify(userRightsService).validateAdminUser(WikipediaLanguage.SPANISH, "A");
        verify(replacementCountService).countNotReviewedGroupedByPage(WikipediaLanguage.SPANISH);
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
        verify(replacementCountService, never()).countNotReviewedGroupedByPage(WikipediaLanguage.SPANISH);
    }
}