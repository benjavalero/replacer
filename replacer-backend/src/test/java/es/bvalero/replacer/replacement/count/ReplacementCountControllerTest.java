package es.bvalero.replacer.replacement.count;

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
@WebMvcTest(controllers = ReplacementCountController.class)
class ReplacementCountControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ReplacementCacheRepository replacementCacheRepository;

    @Test
    void testFindReplacementCount() throws Exception {
        SubtypeCount subCount = SubtypeCount.of("Y", 100L);
        TypeCount count = TypeCount.of("X");
        count.add(subCount);
        Collection<TypeCount> counts = Collections.singletonList(count);
        when(replacementCacheRepository.countReplacementsGroupedByType(WikipediaLanguage.SPANISH)).thenReturn(counts);

        mvc
            .perform(get("/api/replacement-types/count?lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].t", is("X")))
            .andExpect(jsonPath("$[0].l[0].s", is("Y")))
            .andExpect(jsonPath("$[0].l[0].c", is(100)));

        verify(replacementCacheRepository).countReplacementsGroupedByType(WikipediaLanguage.SPANISH);
    }
}
