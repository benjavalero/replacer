package es.bvalero.replacer.wikipedia;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.scribejava.core.model.OAuth1AccessToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = WikipediaController.class)
class WikipediaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private WikipediaService wikipediaService;

    @Test
    void testGetRequestToken() throws Exception {
        when(wikipediaService.getLoggedUserName(any(OAuth1AccessToken.class), any(WikipediaLanguage.class)))
            .thenReturn("A");
        when(wikipediaService.isAdminUser("A")).thenReturn(true);

        mvc
            .perform(
                get("/api/authentication/user?lang=es")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("accessToken", "X")
                    .param("accessTokenSecret", "Y")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("A")))
            .andExpect(jsonPath("$.admin", equalTo(true)));

        verify(wikipediaService, times(1)).isAdminUser("A");
    }
}
