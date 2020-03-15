package es.bvalero.replacer.dump;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = DumpController.class)
public class DumpControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DumpManager dumpManager;

    @Test
    public void testGetDumpStatus() throws Exception {
        boolean running = true;
        boolean forceProcess = true;
        long numArticlesRead = 1000;
        long numArticlesProcessable = 800;
        long numArticlesProcessed = 500;
        String dumpFileName = "xxx.xml.bz2";
        long start = 1500;
        long end = 2000;
        DumpIndexation indexation = new DumpIndexation(running, forceProcess, numArticlesRead, numArticlesProcessable,
                numArticlesProcessed, dumpFileName, start, end);
        when(dumpManager.getDumpStatus()).thenReturn(indexation);

        mvc.perform(get("/api/dump/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running", equalTo(running)))
                .andExpect(jsonPath("$.forceProcess", equalTo(forceProcess)))
                .andExpect(jsonPath("$.numArticlesRead", is(Long.valueOf(numArticlesRead).intValue())))
                .andExpect(jsonPath("$.numArticlesProcessable", is(Long.valueOf(numArticlesProcessable).intValue())))
                .andExpect(jsonPath("$.numArticlesProcessed", is(Long.valueOf(numArticlesProcessed).intValue())))
                .andExpect(jsonPath("$.dumpFileName", is(dumpFileName)))
                .andExpect(jsonPath("$.start", is(Long.valueOf(start).intValue())))
                .andExpect(jsonPath("$.end", is(Long.valueOf(end).intValue())));

        verify(dumpManager, times(1)).getDumpStatus();
    }

    @Test
    public void testProcessLatestDumpFileManually() throws Exception {
        mvc.perform(post("/api/dump/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(dumpManager, times(1)).processLatestDumpFile(eq(false));
    }

    @Test
    public void testProcessLatestDumpFileManuallyForced() throws Exception {
        mvc.perform(post("/api/dump/force")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(dumpManager, times(1)).processLatestDumpFile(eq(true));
    }

}
