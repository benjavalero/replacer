package es.bvalero.replacer.admin;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.bvalero.replacer.WebMvcConfiguration;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.*;
import java.util.Optional;
import javax.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AdminController.class)
@Import({ AopAutoConfiguration.class, ValidateUserAspect.class, WebMvcConfiguration.class })
class AdminControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @MockBean
    private PublicIpService publicIpService;

    @Test
    void testGetPublic() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "x");
        AccessToken accessToken = AccessToken.of("a", "b");
        User user = User.builder().id(userId).accessToken(accessToken).admin(true).build();
        when(userService.findAuthenticatedUser(WikipediaLanguage.getDefault(), accessToken))
            .thenReturn(Optional.of(user));

        String ip = "ip";
        when(publicIpService.getPublicIp()).thenReturn(ip);

        mvc
            .perform(
                get("/api/admin/public-ip")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(new Cookie(AccessToken.COOKIE_NAME, accessToken.toCookieValue()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ip", is(ip)));

        verify(publicIpService).getPublicIp();
    }

    @Test
    void testGetPublicIpNotAdmin() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "x");
        AccessToken accessToken = AccessToken.of("a", "b");
        User user = User.builder().id(userId).accessToken(accessToken).admin(false).build();
        when(userService.findAuthenticatedUser(WikipediaLanguage.getDefault(), accessToken))
            .thenReturn(Optional.of(user));

        mvc
            .perform(
                get("/api/admin/public-ip")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(new Cookie(AccessToken.COOKIE_NAME, accessToken.toCookieValue()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());

        verify(publicIpService, never()).getPublicIp();
    }
}
