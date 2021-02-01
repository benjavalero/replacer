package es.bvalero.replacer.authentication;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import es.bvalero.replacer.config.TestConfiguration;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = { AuthenticationController.class, TestConfiguration.class })
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private WikipediaService wikipediaService;

    @Test
    void testGetRequestToken() throws Exception {
        when(authenticationService.getRequestToken()).thenReturn(new OAuth1RequestToken("X", "Y"));
        when(authenticationService.getAuthorizationUrl(any())).thenReturn("Z");

        mvc
            .perform(get("/api/authentication/request-token").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token", is("X")))
            .andExpect(jsonPath("$.tokenSecret", is("Y")))
            .andExpect(jsonPath("$.authorizationUrl", is("Z")));

        verify(authenticationService, times(1)).getRequestToken();
        verify(authenticationService, times(1)).getAuthorizationUrl(any());
    }

    @Test
    void testGetAccessToken() throws Exception {
        when(authenticationService.getAccessToken(any(OAuth1RequestToken.class), anyString()))
            .thenReturn(new OAuth1AccessToken("A", "B"));
        when(wikipediaService.getLoggedUserName(any(OAuth1AccessToken.class))).thenReturn("C");
        when(wikipediaService.isAdminUser("C")).thenReturn(true);

        mvc
            .perform(
                get("/api/authentication/access-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("requestToken", "X")
                    .param("requestTokenSecret", "Y")
                    .param("oauthVerifier", "V")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("C")))
            .andExpect(jsonPath("$.admin", equalTo(true)))
            .andExpect(jsonPath("$.accessToken.token", is("A")))
            .andExpect(jsonPath("$.accessToken.tokenSecret", is("B")));

        verify(authenticationService, times(1)).getAccessToken(any(), anyString());
        verify(wikipediaService, times(1)).getLoggedUserName(any());
        verify(wikipediaService, times(1)).isAdminUser("C");
    }
}
