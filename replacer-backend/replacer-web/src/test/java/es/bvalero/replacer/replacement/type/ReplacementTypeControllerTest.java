package es.bvalero.replacer.replacement.type;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.WebMvcConfiguration;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.util.WebUtils;
import es.bvalero.replacer.finder.ReplacementKind;
import es.bvalero.replacer.finder.StandardType;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = { ReplacementTypeController.class, WebMvcConfiguration.class })
class ReplacementTypeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebUtils webUtils;

    @MockitoBean
    private ReplacementTypeFindApi replacementTypeFindApi;

    @Test
    void testFindReplacementType() throws Exception {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        when(webUtils.getLanguageHeader(any(HttpServletRequest.class))).thenReturn(lang);

        final String replacement = "Africa";
        when(replacementTypeFindApi.findReplacementType(WikipediaLanguage.getDefault(), replacement, true)).thenReturn(
            Optional.of(StandardType.of(ReplacementKind.SIMPLE, replacement))
        );

        mvc
            .perform(
                get("/api/type?replacement=Africa&cs=true")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.kind", is((int) ReplacementKind.SIMPLE.getCode())))
            .andExpect(jsonPath("$.subtype", is(replacement)));

        verify(replacementTypeFindApi).findReplacementType(WikipediaLanguage.getDefault(), replacement, true);
    }

    @Test
    void testFindReplacementTypeEmpty() throws Exception {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        when(webUtils.getLanguageHeader(any(HttpServletRequest.class))).thenReturn(lang);

        final String replacement = "African";
        when(replacementTypeFindApi.findReplacementType(WikipediaLanguage.getDefault(), replacement, true)).thenReturn(
            Optional.empty()
        );

        mvc
            .perform(
                get("/api/type?replacement=African&cs=true")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$.kind").doesNotExist())
            .andExpect(jsonPath("$.subtype").doesNotExist());

        verify(replacementTypeFindApi).findReplacementType(WikipediaLanguage.getDefault(), replacement, true);
    }
}
