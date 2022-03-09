package es.bvalero.replacer.admin;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ForbiddenException;
import es.bvalero.replacer.user.validate.UserRightsService;
import es.bvalero.replacer.user.validate.ValidateUserAspect;
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

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AdminController.class)
@Import({AopAutoConfiguration.class, ValidateUserAspect.class})
class AdminControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRightsService userRightsService;

    @MockBean
    private PublicIpService publicIpService;

    @Test
    void testGetPublic() throws Exception {
        PublicIp ip = PublicIp.of("x");
        when(publicIpService.getPublicIp()).thenReturn(ip);

        mvc
            .perform(get("/api/admin/public-ip?user=x&lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ip", is(ip.getIp())));

        verify(userRightsService).validateAdminUser(any(WikipediaLanguage.class),anyString());
        verify(publicIpService).getPublicIp();
    }

    @Test
    void testGetPublicIpNotAdmin() throws Exception {
        doThrow(ForbiddenException.class).when(userRightsService).validateAdminUser(any(WikipediaLanguage.class),anyString());

        mvc
            .perform(get("/api/admin/public-ip?user=x&lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        verify(userRightsService).validateAdminUser(any(WikipediaLanguage.class),anyString());
        verify(publicIpService, never()).getPublicIp();
    }
}
