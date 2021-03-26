package es.bvalero.replacer.wikipedia;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AuthenticationController.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WikipediaService wikipediaService;

    @Test
    void testGetRequestToken() throws Exception {
        when(wikipediaService.getRequestToken()).thenReturn(RequestToken.of("X", "Y", "Z"));

        mvc
            .perform(get("/api/authentication/request-token").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token", is("X")))
            .andExpect(jsonPath("$.tokenSecret", is("Y")))
            .andExpect(jsonPath("$.authorizationUrl", is("Z")));

        verify(wikipediaService, times(1)).getRequestToken();
    }

    @Test
    void testGetLoggedUser() throws Exception {
        AccessToken accessToken = AccessToken.of("A", "B");
        String userName = "C";

        when(wikipediaService.getLoggedUser(anyString(), anyString(), anyString()))
            .thenReturn(WikipediaUser.of(userName, true, accessToken));

        VerificationToken verifier = VerificationToken.of("X", "Y", "V");
        mvc
            .perform(
                post("/api/authentication/logged-user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(verifier))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("C")))
            .andExpect(jsonPath("$.admin", equalTo(true)))
            .andExpect(jsonPath("$.accessToken.token", is("A")))
            .andExpect(jsonPath("$.accessToken.tokenSecret", is("B")));

        verify(wikipediaService, times(1)).getLoggedUser(anyString(), anyString(), anyString());
    }
}
