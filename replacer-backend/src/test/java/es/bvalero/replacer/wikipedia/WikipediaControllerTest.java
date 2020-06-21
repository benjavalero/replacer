package es.bvalero.replacer.wikipedia;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.authentication.AccessToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// TODO: Adapt to Junit5
@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = WikipediaController.class)
public class WikipediaControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WikipediaService wikipediaService;

    @Test
    public void testGetRequestToken() throws Exception {
        AccessToken accessToken = new AccessToken("X", "Y");
        when(wikipediaService.getLoggedUserName(any(), any())).thenReturn("A");
        when(wikipediaService.isAdminUser("A")).thenReturn(true);

        mvc.perform(post("/api/wikipedia/username")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("A")))
                .andExpect(jsonPath("$.admin", equalTo(true)));
    }

}
