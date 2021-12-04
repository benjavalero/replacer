package es.bvalero.replacer.replacement;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.replacement.count.LanguageCount;
import es.bvalero.replacer.replacement.count.ReplacementCountRepository;
import es.bvalero.replacer.replacement.count.SubtypeCount;
import es.bvalero.replacer.replacement.count.TypeCount;
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

    @MockBean
    private ReplacementCountRepository replacementCountRepository;

    @Test
    void testCountReplacementsToReview() throws Exception {
        long count = 100;
        when(replacementService.countReplacementsNotReviewed(WikipediaLanguage.SPANISH)).thenReturn(count);

        mvc
            .perform(get("/api/replacements/count?reviewed=false&lang=es"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count", is(Long.valueOf(count).intValue())));

        verify(replacementService).countReplacementsNotReviewed(WikipediaLanguage.SPANISH);
    }

    @Test
    void testCountReplacementsReviewed() throws Exception {
        long count = 100;
        when(replacementService.countReplacementsReviewed(WikipediaLanguage.SPANISH)).thenReturn(count);

        mvc
            .perform(get("/api/replacements/count?reviewed=true&lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count", is(Long.valueOf(count).intValue())));

        verify(replacementService).countReplacementsReviewed(WikipediaLanguage.SPANISH);
    }

    @Test
    void testCountReplacementsGroupedByReviewer() throws Exception {
        ReviewerCount count = ReviewerCount.of("X", 100);
        when(replacementService.countReplacementsGroupedByReviewer(WikipediaLanguage.SPANISH))
            .thenReturn(Collections.singletonList(count));

        mvc
            .perform(get("/api/users/count?lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].reviewer", is("X")))
            .andExpect(jsonPath("$[0].count", is(100)));

        verify(replacementService).countReplacementsGroupedByReviewer(WikipediaLanguage.SPANISH);
    }

    @Test
    void testFindReplacementCount() throws Exception {
        SubtypeCount subCount = SubtypeCount.of("Y", 100);
        TypeCount count = TypeCount.of("X");
        count.add(subCount);
        LanguageCount langCount = mock(LanguageCount.class);
        when(langCount.getTypeCounts()).thenReturn(Collections.singletonList(count));
        when(replacementCountRepository.countReplacementsGroupedByType(WikipediaLanguage.SPANISH))
            .thenReturn(langCount);

        mvc
            .perform(get("/api/replacement-types/count?lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].t", is("X")))
            .andExpect(jsonPath("$[0].l[0].s", is("Y")))
            .andExpect(jsonPath("$[0].l[0].c", is(100)));

        verify(replacementCountRepository).countReplacementsGroupedByType(WikipediaLanguage.SPANISH);
    }
}
