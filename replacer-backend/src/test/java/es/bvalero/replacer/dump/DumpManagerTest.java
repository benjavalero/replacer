package es.bvalero.replacer.dump;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
        Assertions.assertNotNull(dumpFile);
        Assertions.assertTrue(Files.exists(dumpFile));

        dumpJob.parseDumpFile(dumpFile, WikipediaLanguage.SPANISH);

        Mockito.verify(dumpHandler).startDocument();
    }

    @Test
    void testProcessLatestDumpFileWithException() throws Exception {
        Mockito
            .when(dumpFinder.findLatestDumpFile(Mockito.any(WikipediaLanguage.class)))
            .thenThrow(ReplacerException.class);
        Mockito.when(dumpHandler.getDumpIndexingStatus()).thenReturn(DumpIndexingStatus.ofEmpty());

        dumpManager.processLatestDumpFiles();

        Mockito.verify(dumpHandler, Mockito.never()).startDocument();
    }

    @Test
    void testProcessLatestDumpFileAlreadyRunning() {
        // Constructor with arguments to fake the start
        DumpIndexingStatus status = DumpIndexingStatus.of("File", 1);
        Mockito.when(dumpHandler.getDumpIndexingStatus()).thenReturn(status);

        dumpManager.processLatestDumpFiles();

        Mockito.verify(dumpHandler, Mockito.never()).startDocument();
    }

    @Test
    void testProcessDumpScheduled() throws Exception {
        Path dumpFile = Paths.get(
            getClass()
                .getResource("/es/bvalero/replacer/dump/eswiki/20170101/eswiki-20170101-pages-articles.xml.bz2")
                .toURI()
        );
        Mockito.when(dumpFinder.findLatestDumpFile(Mockito.any(WikipediaLanguage.class))).thenReturn(dumpFile);

        // Make the dump file old enough
        Files.setLastModifiedTime(dumpFile, FileTime.from(LocalDateTime.now().minusDays(2).toInstant(ZoneOffset.UTC)));

        Mockito.when(dumpHandler.getDumpIndexingStatus()).thenReturn(DumpIndexingStatus.ofEmpty());

        dumpManager.scheduledStartDumpIndexing();

        // Run twice (for Spanish and Galician)
        Mockito.verify(dumpHandler, Mockito.times(2)).startDocument();
    }
}
