package es.bvalero.replacer.dump;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.DumpProperties;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.page.index.PageIndexBatchService;
import es.bvalero.replacer.page.index.PageIndexResult;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DumpParserTest {

    // Dependency injection
    private PageIndexBatchService pageIndexService;
    private DumpProperties dumpProperties;

    private DumpParser dumpParser;

    @BeforeEach
    public void setUp() {
        pageIndexService = mock(PageIndexBatchService.class);
        dumpProperties = mock(DumpProperties.class);
        dumpParser = new DumpParser(pageIndexService, dumpProperties);
    }

    @Test
    void testParseDumpFile() throws ReplacerException, URISyntaxException {
        // We need a real dump file to create the input stream
        Path dumpFile = Paths.get(
            Objects
                .requireNonNull(
                    getClass()
                        .getResource("/es/bvalero/replacer/dump/eswiki/20170101/eswiki-20170101-pages-articles.xml.bz2")
                )
                .toURI()
        );
        assertNotNull(dumpFile);
        assertTrue(Files.exists(dumpFile));

        assertTrue(dumpParser.getDumpStatus().isEmpty());

        when(pageIndexService.indexPage(any(DumpPage.class)))
            .thenReturn(PageIndexResult.ofIndexed())
            .thenReturn(PageIndexResult.ofNotIndexed())
            .thenReturn(PageIndexResult.ofIndexed())
            .thenReturn(PageIndexResult.ofNotIndexable());
        when(dumpProperties.getNumPagesEstimated()).thenReturn(Map.of(WikipediaLanguage.SPANISH.getCode(), 150000));

        dumpParser.parseDumpFile(WikipediaLanguage.SPANISH, DumpFile.of(dumpFile));

        verify(pageIndexService, times(4)).indexPage(any(DumpPage.class));
        verify(pageIndexService).finish();

        Optional<DumpStatus> dumpStatus = dumpParser.getDumpStatus();
        assertTrue(dumpStatus.isPresent());
        DumpStatus status = dumpStatus.get();
        assertFalse(status.isRunning());
        assertEquals("eswiki-20170101-pages-articles.xml.bz2", status.getDumpFileName());
        assertNotNull(status.getStart());
        assertNotNull(status.getEnd());
        assertEquals(3, status.getNumPagesRead());
        assertEquals(2, status.getNumPagesIndexed());
    }
}
