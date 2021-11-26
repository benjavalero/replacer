package es.bvalero.replacer.dump;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.bvalero.replacer.wikipedia.WikipediaService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = DumpController.class)
class DumpControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private DumpManager dumpManager;

    @MockBean
    private WikipediaService wikipediaService;

    @Test
    void testGetDumpIndexingStatus() throws Exception {
        boolean running = true;
        long numPagesRead = 1000;
        long numPagesIndexed = 500;
        long numPagesEstimated = 200000;
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
        when(wikipediaService.isAdminUser(anyString())).thenReturn(true);
        when(dumpManager.getDumpIndexingStatus()).thenReturn(indexingStatus);

        mvc
            .perform(get("/api/dump-indexing?user=x&lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.running", equalTo(running)))
            .andExpect(jsonPath("$.numPagesRead", is(Long.valueOf(numPagesRead).intValue())))
            .andExpect(jsonPath("$.numPagesIndexed", is(Long.valueOf(numPagesIndexed).intValue())))
            .andExpect(jsonPath("$.numPagesEstimated", is(Long.valueOf(numPagesEstimated).intValue())))
            .andExpect(jsonPath("$.dumpFileName", is(dumpFileName)))
            .andExpect(jsonPath("$.start", is(DumpLocalDateTimeSerializer.convertLocalDateTimeToMilliseconds(start))))
            .andExpect(jsonPath("$.end", is(DumpLocalDateTimeSerializer.convertLocalDateTimeToMilliseconds(end))));

        verify(wikipediaService).isAdminUser(anyString());
        verify(dumpManager).getDumpIndexingStatus();
    }

    @Test
    void testGetDumpIndexingStatusNotAdmin() throws Exception {
        when(wikipediaService.isAdminUser(anyString())).thenReturn(false);

        mvc
            .perform(get("/api/dump-indexing?user=x&lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());

        verify(wikipediaService).isAdminUser(anyString());
        verify(dumpManager, never()).indexLatestDumpFiles();
    }

    @Test
    void testPostStart() throws Exception {
        when(wikipediaService.isAdminUser(anyString())).thenReturn(true);

        mvc
            .perform(post("/api/dump-indexing?user=x&lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(wikipediaService).isAdminUser(anyString());
        verify(dumpManager).indexLatestDumpFiles();
    }

    @Test
    void testPostStartNotAdmin() throws Exception {
        when(wikipediaService.isAdminUser(anyString())).thenReturn(false);

        mvc
            .perform(post("/api/dump-indexing?user=x&lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());

        verify(wikipediaService).isAdminUser(anyString());
        verify(dumpManager, never()).indexLatestDumpFiles();
    }
}
