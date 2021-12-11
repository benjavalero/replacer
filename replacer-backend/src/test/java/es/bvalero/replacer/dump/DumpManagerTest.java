package es.bvalero.replacer.dump;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class DumpManagerTest {

    @Mock
    private DumpFinder dumpFinder;

    @Mock
    private DumpParser dumpParser;

    @InjectMocks
    private DumpManager dumpManager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessLatestDumpFiles() throws ReplacerException {
        when(dumpParser.getDumpIndexingStatus()).thenReturn(DumpIndexingStatus.ofEmpty());

        Path dumpPath = mock(Path.class);
        when(dumpFinder.findLatestDumpFile(any(WikipediaLanguage.class))).thenReturn(dumpPath);

        dumpManager.indexLatestDumpFiles();

        // 2 executions, one per language.
        verify(dumpFinder, times(2)).findLatestDumpFile(any(WikipediaLanguage.class));
        verify(dumpParser, times(2)).parseDumpFile(any(WikipediaLanguage.class), any(Path.class));
    }

    @Test
    void testProcessLatestDumpFilesWithException() throws ReplacerException {
        when(dumpParser.getDumpIndexingStatus()).thenReturn(DumpIndexingStatus.ofEmpty());

        Path dumpPath = mock(Path.class);
        when(dumpFinder.findLatestDumpFile(any(WikipediaLanguage.class))).thenReturn(dumpPath);
        doThrow(ReplacerException.class).when(dumpParser).parseDumpFile(any(WikipediaLanguage.class), any(Path.class));

        dumpManager.indexLatestDumpFiles();

        // 2 executions, one per language.
        verify(dumpFinder, times(2)).findLatestDumpFile(any(WikipediaLanguage.class));
        verify(dumpParser, times(2)).parseDumpFile(any(WikipediaLanguage.class), any(Path.class));
    }

    @Test
    void testProcessLatestDumpFilesAlreadyRunning() throws ReplacerException {
        when(dumpParser.getDumpIndexingStatus()).thenReturn(DumpIndexingStatus.builder().running(true).build());

        dumpManager.indexLatestDumpFiles();

        // 2 executions, one per language.
        verify(dumpFinder, never()).findLatestDumpFile(any(WikipediaLanguage.class));
        verify(dumpParser, never()).parseDumpFile(any(WikipediaLanguage.class), any(Path.class));
    }

    @Test
    void testGetDumpIndexingStatus() {
        LocalDateTime now = LocalDateTime.now();
        DumpIndexingStatus expected = DumpIndexingStatus
            .builder()
            .running(true)
            .dumpFileName("X")
            .numPagesRead(1L)
            .numPagesIndexed(2L)
            .numPagesEstimated(3L)
            .start(now)
            .end(now.plusHours(1))
            .build();

        when(dumpParser.getDumpIndexingStatus()).thenReturn(expected);

        DumpIndexingStatus actual = dumpManager.getDumpIndexingStatus();

        assertEquals(expected, actual);
        verify(dumpParser).getDumpIndexingStatus();
    }
}
