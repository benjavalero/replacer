package es.bvalero.replacer.replacement;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = ReplacementController.class)
public class ReplacementControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReplacementCountService replacementCountService;

    @Test
    public void testCountReplacements() throws Exception {
        long count = 100;
        when(replacementCountService.countAllReplacements()).thenReturn(count);

        mvc.perform(get("/api/replacement/count")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(Long.valueOf(count).intValue())));

        verify(replacementCountService, times(1)).countAllReplacements();
    }

    @Test
    public void testCountReplacementsToReview() throws Exception {
        long count = 100;
        when(replacementCountService.countReplacementsToReview()).thenReturn(count);

        mvc.perform(get("/api/replacement/count/to-review")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(Long.valueOf(count).intValue())));

        verify(replacementCountService, times(1)).countReplacementsToReview();
    }

    @Test
    public void testCountReplacementsReviewed() throws Exception {
        long count = 100;
        when(replacementCountService.countReplacementsReviewed()).thenReturn(count);

        mvc.perform(get("/api/replacement/count/reviewed")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(Long.valueOf(count).intValue())));

        verify(replacementCountService, times(1)).countReplacementsReviewed();
    }

    @Test
    public void testCountReplacementsGroupedByReviewer() throws Exception {
        ReviewerCount count = new ReviewerCount("X", 100);
        when(replacementCountService.countReplacementsGroupedByReviewer()).thenReturn(Collections.singletonList(count));

        mvc.perform(get("/api/replacement/count/reviewed/grouped")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reviewer", is("X")))
                .andExpect(jsonPath("$[0].count", is(100)));

        verify(replacementCountService, times(1)).countReplacementsGroupedByReviewer();
    }

    @Test
    public void testFindReplacementCount() throws Exception {
        SubtypeCount subCount = SubtypeCount.of("Y", 100);
        TypeCount count = TypeCount.of("X", Collections.singletonList(subCount));
        when(replacementCountService.findReplacementCount()).thenReturn(Collections.singletonList(count));

        mvc.perform(get("/api/replacement/count/grouped")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].t", is("X")))
                .andExpect(jsonPath("$[0].l[0].s", is("Y")))
                .andExpect(jsonPath("$[0].l[0].c", is(100)));

        verify(replacementCountService, times(1)).findReplacementCount();
    }

}
