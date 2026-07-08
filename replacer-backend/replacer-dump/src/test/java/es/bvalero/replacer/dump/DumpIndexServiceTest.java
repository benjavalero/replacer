package es.bvalero.replacer.dump;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
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
    void testProcessLatestDumpFiles() {
        when(dumpParser.getDumpStatus()).thenReturn(Optional.empty());

        Path dumpPath = mock(Path.class);
        DumpFile dumpFile = DumpFile.of(dumpPath);
        when(dumpFinder.findLatestDumpFiles(any(WikipediaLanguage.class))).thenReturn(List.of(dumpFile));

        dumpIndexService.indexLatestDumpFiles();

        // 2 executions, one per language.
        verify(dumpFinder, times(2)).findLatestDumpFiles(any(WikipediaLanguage.class));
        verify(dumpParser, times(2)).parseDumpFiles(any(WikipediaLanguage.class), anyCollection());
    }

    @Test
    void testProcessLatestDumpFilesAlreadyRunning() {
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
        verify(dumpFinder, never()).findLatestDumpFiles(any(WikipediaLanguage.class));
        verify(dumpParser, never()).parseDumpFiles(any(WikipediaLanguage.class), anyCollection());
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
