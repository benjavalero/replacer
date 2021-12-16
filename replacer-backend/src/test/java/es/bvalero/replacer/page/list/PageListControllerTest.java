package es.bvalero.replacer.page.list;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = PageListController.class)
class PageListControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PageUnreviewedTitleListService pageUnreviewedTitleListService;

    @MockBean
    private ReviewByTypeService reviewByTypeService;

    @Test
    void testFindPageTitlesToReviewByType() throws Exception {
        mvc
            .perform(
                get("/api/pages?type=Ortografía&subtype=Africa&lang=es&user=A").contentType(MediaType.TEXT_PLAIN_VALUE)
            )
            .andExpect(status().isOk());

        verify(pageUnreviewedTitleListService)
            .findPageTitlesToReviewByType(
                WikipediaLanguage.SPANISH,
                ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "Africa")
            );
    }

    @Test
    void testReviewAsSystemByType() throws Exception {
        mvc
            .perform(
                post("/api/pages/review?type=Ortografía&subtype=Africa&lang=es&user=A")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());

        verify(reviewByTypeService)
            .reviewAsSystemByType(
                WikipediaLanguage.SPANISH,
                ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "Africa")
            );
    }
}
