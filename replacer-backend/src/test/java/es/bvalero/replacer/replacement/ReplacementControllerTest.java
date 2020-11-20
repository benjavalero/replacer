package es.bvalero.replacer.replacement;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaLanguageConverter;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { ReplacementController.class, WikipediaLanguageConverter.class })
public class ReplacementControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReplacementCountService replacementCountService;

    @Test
    public void testCountReplacementsToReview() throws Exception {
        long count = 100;
        when(replacementCountService.countReplacementsNotReviewed(WikipediaLanguage.SPANISH)).thenReturn(count);

        mvc
            .perform(get("/api/replacements/count?reviewed=false&lang=es"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", is(Long.valueOf(count).intValue())));

        verify(replacementCountService, times(1)).countReplacementsNotReviewed(WikipediaLanguage.SPANISH);
    }

    @Test
    public void testCountReplacementsReviewed() throws Exception {
        long count = 100;
        when(replacementCountService.countReplacementsReviewed(WikipediaLanguage.SPANISH)).thenReturn(count);

        mvc
            .perform(get("/api/replacements/count?reviewed=true&lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", is(Long.valueOf(count).intValue())));

        verify(replacementCountService, times(1)).countReplacementsReviewed(WikipediaLanguage.SPANISH);
    }

    @Test
    public void testCountReplacementsGroupedByReviewer() throws Exception {
        ReviewerCount count = new ReviewerCount("X", 100);
        when(replacementCountService.countReplacementsGroupedByReviewer(WikipediaLanguage.SPANISH))
            .thenReturn(Collections.singletonList(count));

        mvc
            .perform(
                get("/api/replacements/count?reviewed=true&grouped&lang=es").contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].reviewer", is("X")))
            .andExpect(jsonPath("$[0].count", is(100)));

        verify(replacementCountService, times(1)).countReplacementsGroupedByReviewer(WikipediaLanguage.SPANISH);
    }

    @Test
    public void testFindReplacementCount() throws Exception {
        SubtypeCount subCount = new SubtypeCount("Y", 100);
        TypeCount count = new TypeCount("X");
        count.add(subCount);
        when(replacementCountService.getCachedReplacementTypeCounts(WikipediaLanguage.SPANISH))
            .thenReturn(Collections.singletonList(count));

        mvc
            .perform(
                get("/api/replacements/count?reviewed=false&grouped&lang=es").contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].t", is("X")))
            .andExpect(jsonPath("$[0].l[0].s", is("Y")))
            .andExpect(jsonPath("$[0].l[0].c", is(100)));

        verify(replacementCountService, times(1)).getCachedReplacementTypeCounts(WikipediaLanguage.SPANISH);
    }
}
