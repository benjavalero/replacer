package es.bvalero.replacer.replacement;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.bvalero.replacer.common.WikipediaLanguage;
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
@WebMvcTest(controllers = ReplacementController.class)
class ReplacementControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ReplacementService replacementService;

    @Test
    void testCountReplacementsToReview() throws Exception {
        long count = 100;
        when(replacementService.countReplacementsNotReviewed(WikipediaLanguage.SPANISH)).thenReturn(count);

        mvc
            .perform(get("/api/replacements/count?reviewed=false&lang=es"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", is(Long.valueOf(count).intValue())));

        verify(replacementService, times(1)).countReplacementsNotReviewed(WikipediaLanguage.SPANISH);
    }

    @Test
    void testCountReplacementsReviewed() throws Exception {
        long count = 100;
        when(replacementService.countReplacementsReviewed(WikipediaLanguage.SPANISH)).thenReturn(count);

        mvc
            .perform(get("/api/replacements/count?reviewed=true&lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", is(Long.valueOf(count).intValue())));

        verify(replacementService, times(1)).countReplacementsReviewed(WikipediaLanguage.SPANISH);
    }

    @Test
    void testCountReplacementsGroupedByReviewer() throws Exception {
        ReviewerCount count = ReviewerCount.of("X", 100);
        when(replacementService.countReplacementsGroupedByReviewer(WikipediaLanguage.SPANISH))
            .thenReturn(Collections.singletonList(count));

        mvc
            .perform(
                get("/api/replacements/count?reviewed=true&grouped&lang=es").contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].reviewer", is("X")))
            .andExpect(jsonPath("$[0].count", is(100)));

        verify(replacementService, times(1)).countReplacementsGroupedByReviewer(WikipediaLanguage.SPANISH);
    }

    @Test
    void testFindReplacementCount() throws Exception {
        SubtypeCount subCount = SubtypeCount.of("Y", 100);
        TypeCount count = TypeCount.of("X");
        count.add(subCount);
        when(replacementService.countReplacementsGroupedByType(WikipediaLanguage.SPANISH))
            .thenReturn(Collections.singletonList(count));

        mvc
            .perform(
                get("/api/replacements/count?reviewed=false&grouped&lang=es").contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].t", is("X")))
            .andExpect(jsonPath("$[0].l[0].s", is("Y")))
            .andExpect(jsonPath("$[0].l[0].c", is(100)));

        verify(replacementService, times(1)).countReplacementsGroupedByType(WikipediaLanguage.SPANISH);
    }
}
