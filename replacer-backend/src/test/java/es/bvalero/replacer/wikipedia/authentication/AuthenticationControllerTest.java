package es.bvalero.replacer.wikipedia.authentication;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.OAuthToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
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
    private AuthenticationService authenticationService;

    @Test
    void testGetRequestToken() throws Exception {
        when(authenticationService.getRequestToken()).thenReturn(RequestToken.of("X", "Y", "Z"));

        mvc
            .perform(get("/api/authentication/request-token").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token", is("X")))
            .andExpect(jsonPath("$.tokenSecret", is("Y")))
            .andExpect(jsonPath("$.authorizationUrl", is("Z")));

        verify(authenticationService, times(1)).getRequestToken();
    }

    @Test
    void testAuthenticate() throws Exception {
        String userName = "C";

        when(
            authenticationService.authenticate(
                Mockito.any(WikipediaLanguage.class),
                Mockito.any(OAuthToken.class),
                anyString()
            )
        )
            .thenReturn(AuthenticateResponse.of(userName, true, false, true, "A", "B"));

        AuthenticateRequest verifier = AuthenticateRequest.of("X", "Y", "V");
        mvc
            .perform(
                post("/api/authentication/authenticate?lang=es")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(verifier))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("C")))
            .andExpect(jsonPath("$.hasRights", equalTo(true)))
            .andExpect(jsonPath("$.bot", equalTo(false)))
            .andExpect(jsonPath("$.admin", equalTo(true)))
            .andExpect(jsonPath("$.token", is("A")))
            .andExpect(jsonPath("$.tokenSecret", is("B")));

        verify(authenticationService, times(1))
            .authenticate(Mockito.eq(WikipediaLanguage.SPANISH), Mockito.any(OAuthToken.class), anyString());
    }
}
