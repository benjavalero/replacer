package es.bvalero.replacer.page.count;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;
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
@WebMvcTest(controllers = PageCountController.class)
class PageCountControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PageCountService pageCountService;

    @Test
    void testFindReplacementCount() throws Exception {
        KindCount count = KindCount.of((byte) 10);
        count.add(SubtypeCount.of("Y", 100));
        Collection<KindCount> counts = Collections.singletonList(count);
        when(pageCountService.countReplacementsGroupedByType(WikipediaLanguage.SPANISH, "A")).thenReturn(counts);

        mvc
            .perform(get("/api/page/type/count?lang=es&user=A").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].k", is(10)))
            .andExpect(jsonPath("$[0].l[0].s", is("Y")))
            .andExpect(jsonPath("$[0].l[0].c", is(100)));

        verify(pageCountService).countReplacementsGroupedByType(WikipediaLanguage.SPANISH, "A");
    }
}
