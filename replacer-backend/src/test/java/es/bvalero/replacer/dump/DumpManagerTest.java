package es.bvalero.replacer.dump;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.domain.ReplacerException;
import es.bvalero.replacer.domain.WikipediaLanguage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

// Test DumpManager and DumpJobSaxImpl at a time
class DumpManagerTest {

    @Mock
    private DumpFinder dumpFinder;

    @InjectMocks
    private DumpManager dumpManager;

    @Mock
    private DumpHandler dumpHandler;

    @InjectMocks
    private DumpJobSaxImpl dumpJob;

    @BeforeEach
    public void setUp() {
        dumpJob = new DumpJobSaxImpl();
        dumpManager = new DumpManager();
        MockitoAnnotations.initMocks(this);
        dumpManager.setDumpJob(dumpJob);
    }

    @Test
    void testParseDumpFile() throws Exception {
        // We need a real dump file to create the input stream
        Path dumpFile = Paths.get(
            getClass()
                .getResource("/es/bvalero/replacer/dump/eswiki/20170101/eswiki-20170101-pages-articles.xml.bz2")
                .toURI()
        );
        assertNotNull(dumpFile);
        assertTrue(Files.exists(dumpFile));

        dumpJob.parseDumpFile(dumpFile, WikipediaLanguage.SPANISH);

        verify(dumpHandler).startDocument();
    }

    @Test
    void testProcessLatestDumpFileWithException() throws Exception {
        when(dumpFinder.findLatestDumpFile(any(WikipediaLanguage.class))).thenThrow(ReplacerException.class);
        when(dumpHandler.getDumpIndexingStatus()).thenReturn(DumpIndexingStatus.ofEmpty());

        dumpManager.processLatestDumpFiles();

        verify(dumpHandler, never()).startDocument();
    }

    @Test
    void testProcessLatestDumpFileAlreadyRunning() {
        // Fake the indexing is running
        DumpIndexingStatus status = DumpIndexingStatus.builder().running(true).build();
        when(dumpHandler.getDumpIndexingStatus()).thenReturn(status);

        dumpManager.processLatestDumpFiles();

        verify(dumpHandler, never()).startDocument();
    }

    @Test
    void testProcessDumpScheduled() throws Exception {
        Path dumpFile = Paths.get(
            getClass()
                .getResource("/es/bvalero/replacer/dump/eswiki/20170101/eswiki-20170101-pages-articles.xml.bz2")
                .toURI()
        );
        when(dumpFinder.findLatestDumpFile(any(WikipediaLanguage.class))).thenReturn(dumpFile);

        // Make the dump file old enough
        Files.setLastModifiedTime(dumpFile, FileTime.from(LocalDateTime.now().minusDays(2).toInstant(ZoneOffset.UTC)));

        when(dumpHandler.getDumpIndexingStatus()).thenReturn(DumpIndexingStatus.ofEmpty());

        dumpManager.scheduledStartDumpIndexing();

        // Run twice (for Spanish and Galician)
        verify(dumpHandler, times(2)).startDocument();
    }
}
