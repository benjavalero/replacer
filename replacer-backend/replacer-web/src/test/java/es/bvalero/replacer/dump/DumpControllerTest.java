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
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.user.*;
import java.time.LocalDateTime;
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
@WebMvcTest(controllers = DumpController.class)
@Import({ AopAutoConfiguration.class, ValidateUserAspect.class, WebMvcConfiguration.class })
class DumpControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private DumpManager dumpManager;

    @MockBean
    private UserService userService;

    @Test
    void testGetDumpIndexingStatus() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "x");
        AccessToken accessToken = AccessToken.of("a", "b");
        User user = User.builder().id(userId).accessToken(accessToken).admin(true).build();
        when(userService.findAuthenticatedUser(WikipediaLanguage.getDefault(), accessToken))
            .thenReturn(Optional.of(user));

        boolean running = true;
        int numPagesRead = 1000;
        int numPagesIndexed = 500;
        int numPagesEstimated = 200000;
        String dumpFileName = "xxx.xml.bz2";
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);
        DumpIndexingStatus indexingStatus = new DumpIndexingStatus(
            running,
            numPagesRead,
            numPagesIndexed,
            numPagesEstimated,
            dumpFileName,
            start,
            end
        );
        when(dumpManager.getDumpIndexingStatus()).thenReturn(indexingStatus);

        mvc
            .perform(
                get("/api/dump-indexing")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(new Cookie(AccessToken.COOKIE_NAME, accessToken.toCookieValue()))
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

        verify(dumpManager).getDumpIndexingStatus();
    }

    @Test
    void testGetDumpIndexingStatusNotAdmin() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "x");
        AccessToken accessToken = AccessToken.of("a", "b");
        User user = User.builder().id(userId).accessToken(accessToken).admin(false).build();
        when(userService.findAuthenticatedUser(WikipediaLanguage.getDefault(), accessToken))
            .thenReturn(Optional.of(user));

        mvc
            .perform(
                get("/api/dump-indexing")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(new Cookie(AccessToken.COOKIE_NAME, accessToken.toCookieValue()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());

        verify(dumpManager, never()).indexLatestDumpFiles();
    }

    @Test
    void testPostStart() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "x");
        AccessToken accessToken = AccessToken.of("a", "b");
        User user = User.builder().id(userId).accessToken(accessToken).admin(true).build();
        when(userService.findAuthenticatedUser(WikipediaLanguage.getDefault(), accessToken))
            .thenReturn(Optional.of(user));

        mvc
            .perform(
                post("/api/dump-indexing")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(new Cookie(AccessToken.COOKIE_NAME, accessToken.toCookieValue()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isAccepted());

        verify(dumpManager).indexLatestDumpFiles();
    }

    @Test
    void testPostStartNotAdmin() throws Exception {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "x");
        AccessToken accessToken = AccessToken.of("a", "b");
        User user = User.builder().id(userId).accessToken(accessToken).admin(false).build();
        when(userService.findAuthenticatedUser(WikipediaLanguage.getDefault(), accessToken))
            .thenReturn(Optional.of(user));

        mvc
            .perform(
                post("/api/dump-indexing")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .cookie(new Cookie(AccessToken.COOKIE_NAME, accessToken.toCookieValue()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());

        verify(dumpManager, never()).indexLatestDumpFiles();
    }
}
