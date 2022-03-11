package es.bvalero.replacer.dump;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ForbiddenException;
import java.time.LocalDateTime;

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
@WebMvcTest(controllers = DumpController.class)
@Import({AopAutoConfiguration.class, ValidateUserAspect.class})
class DumpControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private DumpManager dumpManager;

    @MockBean
    private UserRightsService userRightsService;

    @Test
    void testGetDumpIndexingStatus() throws Exception {
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
            .perform(get("/api/dump-indexing?user=x&lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.running", equalTo(running)))
            .andExpect(jsonPath("$.numPagesRead", is(numPagesRead)))
            .andExpect(jsonPath("$.numPagesIndexed", is(numPagesIndexed)))
            .andExpect(jsonPath("$.numPagesEstimated", is(numPagesEstimated)))
            .andExpect(jsonPath("$.dumpFileName", is(dumpFileName)))
            .andExpect(jsonPath("$.start", is(DumpLocalDateTimeSerializer.convertLocalDateTimeToMilliseconds(start))))
            .andExpect(jsonPath("$.end", is(DumpLocalDateTimeSerializer.convertLocalDateTimeToMilliseconds(end))));

        verify(dumpManager).getDumpIndexingStatus();
    }

    @Test
    void testGetDumpIndexingStatusNotAdmin() throws Exception {
        doThrow(ForbiddenException.class).when(userRightsService).validateAdminUser(
            any(WikipediaLanguage.class),
            anyString());

        mvc
            .perform(get("/api/dump-indexing?user=x&lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        verify(userRightsService).validateAdminUser(any(WikipediaLanguage.class),anyString());
        verify(dumpManager, never()).indexLatestDumpFiles();
    }

    @Test
    void testPostStart() throws Exception {
        mvc
            .perform(post("/api/dump-indexing?user=x&lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isAccepted());

        verify(dumpManager).indexLatestDumpFiles();
    }

    @Test
    void testPostStartNotAdmin() throws Exception {
        doThrow(ForbiddenException.class).when(userRightsService).validateAdminUser(any(WikipediaLanguage.class),anyString());

        mvc
            .perform(post("/api/dump-indexing?user=x&lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        verify(userRightsService).validateAdminUser(any(WikipediaLanguage.class),anyString());
        verify(dumpManager, never()).indexLatestDumpFiles();
    }
}
