package es.bvalero.replacer.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// TODO: Adapt to Junit5
@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {AuthenticationController.class, ModelMapper.class})
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    public void testGetRequestToken() throws Exception {
        when(authenticationService.getRequestToken())
                .thenReturn(new OAuth1RequestToken("X", "Y"));
        when(authenticationService.getAuthorizationUrl(any())).thenReturn("Z");

        mvc.perform(get("/api/authentication/requestToken")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("X")))
                .andExpect(jsonPath("$.tokenSecret", is("Y")))
                .andExpect(jsonPath("$.url", is("Z")));

        verify(authenticationService, times(1)).getRequestToken();
        verify(authenticationService, times(1)).getAuthorizationUrl(any());
    }

    @Test
    public void testGetAccessToken() throws Exception {
        RequestToken requestToken = new RequestToken("X", "Y", "Z");
        VerificationToken verificationToken = new VerificationToken(requestToken, "V");
        when(authenticationService.getAccessToken(any(OAuth1RequestToken.class), anyString()))
                .thenReturn(new OAuth1AccessToken("A", "B"));

        mvc.perform(post("/api/authentication/accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("A")))
                .andExpect(jsonPath("$.tokenSecret", is("B")));

        verify(authenticationService, times(1)).getAccessToken(any(), anyString());
    }

}
