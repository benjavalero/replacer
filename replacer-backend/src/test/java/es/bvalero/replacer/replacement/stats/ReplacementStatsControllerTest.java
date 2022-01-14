package es.bvalero.replacer.replacement.stats;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.ResultCount;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ReplacementStatsController.class)
class ReplacementStatsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ReplacementStatsService replacementStatsService;

    @Test
    void testCountReplacementsToReview() throws Exception {
        int count = 100;
        when(replacementStatsService.countReplacementsNotReviewed(WikipediaLanguage.SPANISH)).thenReturn(count);

        mvc
            .perform(get("/api/replacements/count?reviewed=false&lang=es&user=A"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count", is(count)));

        verify(replacementStatsService).countReplacementsNotReviewed(WikipediaLanguage.SPANISH);
    }

    @Test
    void testCountReplacementsReviewed() throws Exception {
        int count = 100;
        when(replacementStatsService.countReplacementsReviewed(WikipediaLanguage.SPANISH)).thenReturn(count);

        mvc
            .perform(
                get("/api/replacements/count?reviewed=true&lang=es&user=A").contentType(MediaType.APPLICATION_JSON)
            )
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
            .perform(get("/api/users/count?lang=es&user=A").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].reviewer", is("X")))
            .andExpect(jsonPath("$[0].count", is(100)));

        verify(replacementStatsService).countReplacementsGroupedByReviewer(WikipediaLanguage.SPANISH);
    }
}
