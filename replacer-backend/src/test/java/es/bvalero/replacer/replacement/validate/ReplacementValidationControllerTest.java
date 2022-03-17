package es.bvalero.replacer.replacement.validate;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ReplacementValidationController.class)
class ReplacementValidationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReplacementFinderService replacementFinderService;

    @Test
    void testValidateCustomReplacement() throws Exception {
        final String replacement = "Africa";
        when(replacementFinderService.findMatchingReplacementType(WikipediaLanguage.SPANISH, replacement, true))
            .thenReturn(Optional.of(ReplacementType.of(ReplacementKind.SIMPLE, replacement)));

        mvc
            .perform(
                get("/api/replacement/type/validate?replacement=Africa&cs=true&lang=es&user=A")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.kind", is(Byte.valueOf(ReplacementKind.SIMPLE.getCode()).intValue())))
            .andExpect(jsonPath("$.subtype", is(replacement)));

        verify(replacementFinderService).findMatchingReplacementType(WikipediaLanguage.SPANISH, replacement, true);
    }

    @Test
    void testValidateCustomReplacementEmpty() throws Exception {
        final String replacement = "African";
        when(replacementFinderService.findMatchingReplacementType(WikipediaLanguage.SPANISH, replacement, true))
            .thenReturn(Optional.empty());

        mvc
            .perform(
                get("/api/replacement/type/validate?replacement=African&cs=true&lang=es&user=A")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$.kind").doesNotExist())
            .andExpect(jsonPath("$.subtype").doesNotExist());

        verify(replacementFinderService).findMatchingReplacementType(WikipediaLanguage.SPANISH, replacement, true);
    }
}
