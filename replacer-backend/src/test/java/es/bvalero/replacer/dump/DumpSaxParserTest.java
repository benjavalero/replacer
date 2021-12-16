package es.bvalero.replacer.dump;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.page.index.PageIndexResult;
import es.bvalero.replacer.page.index.PageIndexStatus;
import es.bvalero.replacer.page.index.PageIndexService;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class DumpSaxParserTest {

    @Mock
    private PageIndexService pageIndexService;

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

        when(pageIndexService.indexPage(any(WikipediaPage.class)))
            .thenReturn(PageIndexResult.ofEmpty(PageIndexStatus.PAGE_INDEXED, Collections.emptyList()))
            .thenReturn(PageIndexResult.ofEmpty(PageIndexStatus.PAGE_NOT_INDEXED, Collections.emptyList()))
            .thenReturn(PageIndexResult.ofEmpty(PageIndexStatus.PAGE_INDEXED, Collections.emptyList()))
            .thenReturn(PageIndexResult.ofEmpty(PageIndexStatus.PAGE_NOT_INDEXABLE, Collections.emptyList()));

        dumpParser.parseDumpFile(WikipediaLanguage.SPANISH, DumpFile.of(dumpFile));

        assertFalse(dumpParser.getDumpIndexingStatus().getRunning());
        verify(pageIndexService, times(4)).indexPage(any(WikipediaPage.class));
        verify(pageIndexService).finish();

        status = dumpParser.getDumpIndexingStatus();
        assertFalse(status.getRunning());
        assertEquals("eswiki-20170101-pages-articles.xml.bz2", status.getDumpFileName());
        assertNotNull(status.getStart());
        assertNotNull(status.getEnd());
        assertEquals(3, status.getNumPagesRead());
        assertEquals(2, status.getNumPagesIndexed());
    }
}
