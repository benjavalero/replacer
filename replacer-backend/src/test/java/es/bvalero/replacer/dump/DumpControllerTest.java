package es.bvalero.replacer.dump;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
    void testGetDumpIndexingStatus() throws Exception {
        boolean running = true;
        long numPagesRead = 1000;
        long numPagesProcessed = 500;
        long numPagesEstimated = 200000;
        String dumpFileName = "xxx.xml.bz2";
        long start = 1500;
        long end = 2000;
        DumpIndexingStatus indexation = new DumpIndexingStatus(
            running,
            numPagesRead,
            numPagesProcessed,
            numPagesEstimated,
            dumpFileName,
            start,
            end
        );
        when(dumpManager.getDumpIndexingStatus()).thenReturn(indexation);

        mvc
            .perform(get("/api/dump-indexing/status").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.running", equalTo(running)))
            .andExpect(jsonPath("$.numPagesRead", is(Long.valueOf(numPagesRead).intValue())))
            .andExpect(jsonPath("$.numPagesProcessed", is(Long.valueOf(numPagesProcessed).intValue())))
            .andExpect(jsonPath("$.numPagesEstimated", is(Long.valueOf(numPagesEstimated).intValue())))
            .andExpect(jsonPath("$.dumpFileName", is(dumpFileName)))
            .andExpect(jsonPath("$.start", is(Long.valueOf(start).intValue())))
            .andExpect(jsonPath("$.end", is(Long.valueOf(end).intValue())));

        verify(dumpManager, times(1)).getDumpIndexingStatus();
    }

    @Test
    void testPostStart() throws Exception {
        mvc
            .perform(post("/api/dump-indexing/start").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(dumpManager, times(1)).processLatestDumpFiles();
    }
}
