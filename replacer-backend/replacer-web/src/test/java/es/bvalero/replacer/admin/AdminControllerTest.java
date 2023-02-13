package es.bvalero.replacer.admin;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ForbiddenException;
import es.bvalero.replacer.user.UserId;
import es.bvalero.replacer.user.UserRightsService;
import es.bvalero.replacer.user.ValidateUserAspect;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AdminController.class)
@Import({ AopAutoConfiguration.class, ValidateUserAspect.class })
class AdminControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRightsService userRightsService;

    @MockBean
    private PublicIpService publicIpService;

    @Test
    void testGetPublic() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.SPANISH, "x");
        String ip = "ip";
        when(publicIpService.getPublicIp()).thenReturn(ip);

        mvc
            .perform(get("/api/admin/public-ip?user=x&lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ip", is(ip)));

        verify(userRightsService).validateAdminUser(userId);
        verify(publicIpService).getPublicIp();
    }

    @Test
    void testGetPublicIpNotAdmin() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.SPANISH, "x");
        doThrow(ForbiddenException.class).when(userRightsService).validateAdminUser(userId);

        mvc
            .perform(get("/api/admin/public-ip?user=x&lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        verify(userRightsService).validateAdminUser(userId);
        verify(publicIpService, never()).getPublicIp();
    }
}
