package es.bvalero.replacer.dump;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.index.PageIndexResult;
import es.bvalero.replacer.index.PageIndexService;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class DumpSaxParserTest {

    @Mock
    private PageIndexService pageIndexService;

    @Mock
    private Map<String, Integer> numPagesEstimated;

    @InjectMocks
    private DumpSaxParser dumpParser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
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
        when(numPagesEstimated.get(WikipediaLanguage.SPANISH.getCode())).thenReturn(150000);

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
