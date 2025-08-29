package es.bvalero.replacer.dump;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DumpIndexServiceTest {

    // Dependency injection
    private DumpFinder dumpFinder;
    private DumpParser dumpParser;

    private DumpIndexService dumpIndexService;

    @BeforeEach
    public void setUp() {
        dumpFinder = mock(DumpFinder.class);
        dumpParser = mock(DumpParser.class);
        dumpIndexService = new DumpIndexService(dumpFinder, dumpParser);
    }

    @Test
    void testProcessLatestDumpFiles() throws ReplacerException {
        when(dumpParser.getDumpStatus()).thenReturn(Optional.empty());

        Path dumpPath = mock(Path.class);
        DumpFile dumpFile = DumpFile.of(dumpPath);
        when(dumpFinder.findLatestDumpFile(any(WikipediaLanguage.class))).thenReturn(Optional.of(dumpFile));

        dumpIndexService.indexLatestDumpFiles();

        // 2 executions, one per language.
        verify(dumpFinder, times(2)).findLatestDumpFile(any(WikipediaLanguage.class));
        verify(dumpParser, times(2)).parseDumpFile(any(WikipediaLanguage.class), any(DumpFile.class));
    }

    @Test
    void testProcessLatestDumpFilesWithException() throws ReplacerException {
        when(dumpParser.getDumpStatus()).thenReturn(Optional.empty());

        Path dumpPath = mock(Path.class);
        DumpFile dumpFile = DumpFile.of(dumpPath);
        when(dumpFinder.findLatestDumpFile(any(WikipediaLanguage.class))).thenReturn(Optional.of(dumpFile));
        doThrow(ReplacerException.class)
            .when(dumpParser)
            .parseDumpFile(any(WikipediaLanguage.class), any(DumpFile.class));

        dumpIndexService.indexLatestDumpFiles();

        // 2 executions, one per language.
        verify(dumpFinder, times(2)).findLatestDumpFile(any(WikipediaLanguage.class));
        verify(dumpParser, times(2)).parseDumpFile(any(WikipediaLanguage.class), any(DumpFile.class));
    }

    @Test
    void testProcessLatestDumpFilesAlreadyRunning() throws ReplacerException {
        Optional<DumpStatus> dumpStatus = Optional.of(
            DumpStatus.builder()
                .running(true)
                .dumpFileName("X")
                .numPagesRead(1)
                .numPagesIndexed(2)
                .numPagesEstimated(3)
                .start(LocalDateTime.now(ZoneId.systemDefault()))
                .build()
        );
        when(dumpParser.getDumpStatus()).thenReturn(dumpStatus);

        dumpIndexService.indexLatestDumpFiles();

        // 2 executions, one per language.
        verify(dumpFinder, never()).findLatestDumpFile(any(WikipediaLanguage.class));
        verify(dumpParser, never()).parseDumpFile(any(WikipediaLanguage.class), any(DumpFile.class));
    }

    @Test
    void testGetDumpIndexingStatus() {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        Optional<DumpStatus> expected = Optional.of(
            DumpStatus.builder()
                .running(true)
                .dumpFileName("X")
                .numPagesRead(1)
                .numPagesIndexed(2)
                .numPagesEstimated(3)
                .start(now)
                .end(now.plusHours(1))
                .build()
        );

        when(dumpParser.getDumpStatus()).thenReturn(expected);

        Optional<DumpStatus> actual = dumpIndexService.getDumpStatus();

        assertEquals(expected, actual);
        verify(dumpParser).getDumpStatus();
    }
}
