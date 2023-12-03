package es.bvalero.replacer.dump;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.bvalero.replacer.WebMvcConfiguration;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.security.ValidateUserAspect;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.user.User;
import es.bvalero.replacer.user.WebUtils;
import java.time.LocalDateTime;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
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
@WebMvcTest(controllers = DumpController.class)
@Import({ AopAutoConfiguration.class, ValidateUserAspect.class, WebMvcConfiguration.class })
class DumpControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private WebUtils webUtils;

    @MockBean
    private DumpIndexService dumpIndexService;

    @Test
    void testGetDumpStatus() throws Exception {
        User user = User.buildTestAdminUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        boolean running = true;
        int numPagesRead = 1000;
        int numPagesIndexed = 500;
        int numPagesEstimated = 200000;
        String dumpFileName = "xxx.xml.bz2";
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);
        DumpStatus dumpStatus = new DumpStatus(
            running,
            numPagesRead,
            numPagesIndexed,
            numPagesEstimated,
            dumpFileName,
            start,
            end
        );
        when(dumpIndexService.getDumpStatus()).thenReturn(Optional.of(dumpStatus));

        mvc
            .perform(
                get("/api/dump")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.running", equalTo(running)))
            .andExpect(jsonPath("$.numPagesRead", is(numPagesRead)))
            .andExpect(jsonPath("$.numPagesIndexed", is(numPagesIndexed)))
            .andExpect(jsonPath("$.numPagesEstimated", is(numPagesEstimated)))
            .andExpect(jsonPath("$.dumpFileName", is(dumpFileName)))
            .andExpect(jsonPath("$.start", is(ReplacerUtils.convertLocalDateTimeToMilliseconds(start))))
            .andExpect(jsonPath("$.end", is(ReplacerUtils.convertLocalDateTimeToMilliseconds(end))));

        verify(dumpIndexService).getDumpStatus();
    }

    @Test
    void testGetDumpStatusEmpty() throws Exception {
        User user = User.buildTestAdminUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        when(dumpIndexService.getDumpStatus()).thenReturn(Optional.empty());

        mvc
            .perform(
                get("/api/dump")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());

        verify(dumpIndexService).getDumpStatus();
    }

    @Test
    void testGetDumpStatusNotAdmin() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        mvc
            .perform(
                get("/api/dump")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());

        verify(dumpIndexService, never()).indexLatestDumpFiles();
    }

    @Test
    void testPostStart() throws Exception {
        User user = User.buildTestAdminUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        mvc
            .perform(
                post("/api/dump")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isAccepted());

        verify(dumpIndexService).indexLatestDumpFiles();
    }

    @Test
    void testPostStartNotAdmin() throws Exception {
        User user = User.buildTestUser();
        when(webUtils.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);

        mvc
            .perform(
                post("/api/dump")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(WebUtils.buildAccessTokenCookie(user.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());

        verify(dumpIndexService, never()).indexLatestDumpFiles();
    }
}
