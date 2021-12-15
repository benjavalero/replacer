package es.bvalero.replacer.dump;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.page.index.PageIndexStatus;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class DumpSaxParserTest {

    @Mock
    private DumpPageIndexer dumpPageIndexer;

    @Mock
    private Map<String, Long> numPagesEstimated;

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

        DumpIndexingStatus status = dumpParser.getDumpIndexingStatus();
        assertFalse(status.getRunning());
        assertNull(status.getDumpFileName());
        assertNull(status.getStart());
        assertNull(status.getEnd());
        assertNull(status.getNumPagesRead());
        assertNull(status.getNumPagesIndexed());

        when(dumpPageIndexer.index(any(DumpPage.class)))
            .thenReturn(PageIndexStatus.PAGE_INDEXED)
            .thenReturn(PageIndexStatus.PAGE_NOT_INDEXED)
            .thenReturn(PageIndexStatus.PAGE_INDEXED)
            .thenReturn(PageIndexStatus.PAGE_NOT_INDEXABLE);

        dumpParser.parseDumpFile(WikipediaLanguage.SPANISH, DumpFile.of(dumpFile));

        assertFalse(dumpParser.getDumpIndexingStatus().getRunning());
        verify(dumpPageIndexer, times(4)).index(any(DumpPage.class));
        verify(dumpPageIndexer).finish();

        status = dumpParser.getDumpIndexingStatus();
        assertFalse(status.getRunning());
        assertEquals("eswiki-20170101-pages-articles.xml.bz2", status.getDumpFileName());
        assertNotNull(status.getStart());
        assertNotNull(status.getEnd());
        assertEquals(3, status.getNumPagesRead());
        assertEquals(2, status.getNumPagesIndexed());
    }
}
