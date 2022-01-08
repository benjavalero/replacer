package es.bvalero.replacer.page.list;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.bvalero.replacer.authentication.userrights.CheckUserRightsService;
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
    private CheckUserRightsService checkUserRightsService;

    @MockBean
    private PageUnreviewedTitleListService pageUnreviewedTitleListService;

    @MockBean
    private ReviewByTypeService reviewByTypeService;

    @Test
    void testFindPageTitlesToReviewByType() throws Exception {
        when(checkUserRightsService.isBot(WikipediaLanguage.SPANISH, "A")).thenReturn(true);

        mvc
            .perform(
                get("/api/pages?type=Ortografía&subtype=Africa&lang=es&user=A").contentType(MediaType.TEXT_PLAIN_VALUE)
            )
            .andExpect(status().isOk());

        verify(checkUserRightsService).isBot(WikipediaLanguage.SPANISH, "A");
        verify(pageUnreviewedTitleListService)
            .findPageTitlesToReviewByType(
                WikipediaLanguage.SPANISH,
                ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "Africa")
            );
    }

    @Test
    void testFindPageTitlesToReviewByTypeNotBot() throws Exception {
        when(checkUserRightsService.isBot(WikipediaLanguage.SPANISH, "A")).thenReturn(false);

        mvc
            .perform(
                get("/api/pages?type=Ortografía&subtype=Africa&lang=es&user=A").contentType(MediaType.TEXT_PLAIN_VALUE)
            )
            .andExpect(status().isForbidden());

        verify(checkUserRightsService).isBot(WikipediaLanguage.SPANISH, "A");
        verify(pageUnreviewedTitleListService, never())
            .findPageTitlesToReviewByType(
                WikipediaLanguage.SPANISH,
                ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "Africa")
            );
    }

    @Test
    void testReviewAsSystemByType() throws Exception {
        when(checkUserRightsService.isBot(WikipediaLanguage.SPANISH, "A")).thenReturn(true);

        mvc
            .perform(
                post("/api/pages/review?type=Ortografía&subtype=Africa&lang=es&user=A")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());

        verify(checkUserRightsService).isBot(WikipediaLanguage.SPANISH, "A");
        verify(reviewByTypeService)
            .reviewAsSystemByType(
                WikipediaLanguage.SPANISH,
                ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "Africa")
            );
    }

    @Test
    void testReviewAsSystemByTypeNotBot() throws Exception {
        when(checkUserRightsService.isBot(WikipediaLanguage.SPANISH, "A")).thenReturn(false);

        mvc
            .perform(
                post("/api/pages/review?type=Ortografía&subtype=Africa&lang=es&user=A")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());

        verify(checkUserRightsService).isBot(WikipediaLanguage.SPANISH, "A");
        verify(reviewByTypeService, never())
            .reviewAsSystemByType(
                WikipediaLanguage.SPANISH,
                ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "Africa")
            );
    }
}
