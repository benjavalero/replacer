package es.bvalero.replacer.replacement;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.ReplacementTypeMatchService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ReplacementValidationController.class)
class ReplacementValidationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReplacementTypeMatchService replacementTypeMatchService;

    @Test
    void testValidateCustomReplacement() throws Exception {
        final String replacement = "Africa";
        when(replacementTypeMatchService.findMatchingReplacementType(WikipediaLanguage.SPANISH, replacement, true))
            .thenReturn(Optional.of(StandardType.of(ReplacementKind.SIMPLE, replacement)));

        mvc
            .perform(
                get("/api/replacement/type/validate?replacement=Africa&cs=true&lang=es&user=A")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.kind", is(Byte.valueOf(ReplacementKind.SIMPLE.getCode()).intValue())))
            .andExpect(jsonPath("$.subtype", is(replacement)));

        verify(replacementTypeMatchService).findMatchingReplacementType(WikipediaLanguage.SPANISH, replacement, true);
    }

    @Test
    void testValidateCustomReplacementEmpty() throws Exception {
        final String replacement = "African";
        when(replacementTypeMatchService.findMatchingReplacementType(WikipediaLanguage.SPANISH, replacement, true))
            .thenReturn(Optional.empty());

        mvc
            .perform(
                get("/api/replacement/type/validate?replacement=African&cs=true&lang=es&user=A")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$.kind").doesNotExist())
            .andExpect(jsonPath("$.subtype").doesNotExist());

        verify(replacementTypeMatchService).findMatchingReplacementType(WikipediaLanguage.SPANISH, replacement, true);
    }
}
