package es.bvalero.replacer.dump;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
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
        when(dumpParser.getDumpStatus()).thenReturn(DumpStatus.ofEmpty());

        Path dumpPath = mock(Path.class);
        DumpFile dumpFile = DumpFile.of(dumpPath);
        when(dumpFinder.findLatestDumpFile(any(WikipediaLanguage.class))).thenReturn(Optional.of(dumpFile));

        dumpManager.indexLatestDumpFiles();

        // 2 executions, one per language.
        verify(dumpFinder, times(2)).findLatestDumpFile(any(WikipediaLanguage.class));
        verify(dumpParser, times(2)).parseDumpFile(any(WikipediaLanguage.class), any(DumpFile.class));
    }

    @Test
    void testProcessLatestDumpFilesWithException() throws ReplacerException {
        when(dumpParser.getDumpStatus()).thenReturn(DumpStatus.ofEmpty());

        Path dumpPath = mock(Path.class);
        DumpFile dumpFile = DumpFile.of(dumpPath);
        when(dumpFinder.findLatestDumpFile(any(WikipediaLanguage.class))).thenReturn(Optional.of(dumpFile));
        doThrow(ReplacerException.class)
            .when(dumpParser)
            .parseDumpFile(any(WikipediaLanguage.class), any(DumpFile.class));

        dumpManager.indexLatestDumpFiles();

        // 2 executions, one per language.
        verify(dumpFinder, times(2)).findLatestDumpFile(any(WikipediaLanguage.class));
        verify(dumpParser, times(2)).parseDumpFile(any(WikipediaLanguage.class), any(DumpFile.class));
    }

    @Test
    void testProcessLatestDumpFilesAlreadyRunning() throws ReplacerException {
        when(dumpParser.getDumpStatus()).thenReturn(DumpStatus.builder().running(true).build());

        dumpManager.indexLatestDumpFiles();

        // 2 executions, one per language.
        verify(dumpFinder, never()).findLatestDumpFile(any(WikipediaLanguage.class));
        verify(dumpParser, never()).parseDumpFile(any(WikipediaLanguage.class), any(DumpFile.class));
    }

    @Test
    void testGetDumpIndexingStatus() {
        LocalDateTime now = LocalDateTime.now();
        DumpStatus expected = DumpStatus
            .builder()
            .running(true)
            .dumpFileName("X")
            .numPagesRead(1)
            .numPagesIndexed(2)
            .numPagesEstimated(3)
            .start(now)
            .end(now.plusHours(1))
            .build();

        when(dumpParser.getDumpStatus()).thenReturn(expected);

        DumpStatus actual = dumpManager.getDumpStatus();

        assertEquals(expected, actual);
        verify(dumpParser).getDumpStatus();
    }
}
