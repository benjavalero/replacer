package es.bvalero.replacer.dump;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class DumpHandlerTest {

    @Mock
    private IndexationRepository indexationRepository;

    @Mock
    private DumpArticleProcessor dumpArticleProcessor;

    @Spy
    private ModelMapper modelMapper;

    @InjectMocks
    private DumpHandler dumpHandler;

    @Before
    public void setUp() {
        dumpHandler = new DumpHandler();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHandleDumpFile() throws URISyntaxException {
        Path dumpFile = Paths.get(getClass().getResource("/es/bvalero/replacer/dump/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());

        // The dump contains 4 pages: 1 article, 1 annex, 1 redirection and 1 from other category.
        // The first article is not processed. The rest are.
        Mockito.when(dumpArticleProcessor.processArticle(Mockito.any(DumpArticle.class)))
                .thenReturn(Boolean.FALSE).thenReturn(Boolean.TRUE);

        // Start the instance of the handler as done in DumpManager
        try (InputStream xmlInput = new BZip2CompressorInputStream(Files.newInputStream(dumpFile))) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            SAXParser saxParser = factory.newSAXParser();

            dumpHandler.setLatestDumpFile(dumpFile);
            dumpHandler.setForceProcess(false);
            saxParser.parse(xmlInput, dumpHandler);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            // Do nothing
        }

        DumpIndexation status = dumpHandler.getProcessStatus();

        // Check that the execution as finished
        Assert.assertFalse(status.isRunning());
        Assert.assertFalse(status.isForceProcess());
        Assert.assertTrue(status.getStart() > 0);
        Assert.assertNotNull(status.getEnd());
        Assert.assertEquals(dumpFile.getFileName().toString(), status.getDumpFileName());

        // Check number of articles read and processed (we have mocked the results from the processor)
        Assert.assertEquals(4L, status.getNumArticlesRead());
        Assert.assertEquals(2L, status.getNumArticlesProcessable());
        Assert.assertEquals(1L, status.getNumArticlesProcessed());

        Mockito.verify(dumpArticleProcessor, Mockito.times(1)).finishOverallProcess();
    }

    @Test
    public void testHandleDumpFileWithProcessingException() throws URISyntaxException {
        Path dumpFile = Paths.get(getClass().getResource("/es/bvalero/replacer/dump/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());

        // The dump contains 4 pages: 1 article, 1 annex, 1 redirection and 1 from other category.
        // No article is not processed. It throws an exception.
        Mockito.when(dumpArticleProcessor.processArticle(Mockito.any(DumpArticle.class)))
                .thenThrow(Mockito.mock(NullPointerException.class));

        // Start the instance of the handler as done in DumpManager
        try (InputStream xmlInput = new BZip2CompressorInputStream(Files.newInputStream(dumpFile))) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            SAXParser saxParser = factory.newSAXParser();

            dumpHandler.setLatestDumpFile(dumpFile);
            dumpHandler.setForceProcess(false);
            saxParser.parse(xmlInput, dumpHandler);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            // Do nothing
        }

        DumpIndexation status = dumpHandler.getProcessStatus();

        // Check that the execution as finished
        Assert.assertFalse(status.isRunning());
        Assert.assertFalse(status.isForceProcess());
        Assert.assertTrue(status.getStart() > 0);
        Assert.assertNotNull(status.getEnd());
        Assert.assertEquals(dumpFile.getFileName().toString(), status.getDumpFileName());

        // Check number of articles read and processed (we have mocked the results from the processor)
        Assert.assertEquals(4L, status.getNumArticlesRead());
        Assert.assertEquals(2L, status.getNumArticlesProcessable());
        Assert.assertEquals(0L, status.getNumArticlesProcessed());
    }

    @Test
    public void testProcessDefaultStatistics() {
        // Default statistics
        DumpIndexation defaultStats = dumpHandler.getProcessStatus();
        Assert.assertFalse(defaultStats.isRunning());
        Assert.assertFalse(defaultStats.isForceProcess());
        Assert.assertEquals(0L, defaultStats.getNumArticlesRead());
        Assert.assertEquals(0L, defaultStats.getNumArticlesProcessable());
        Assert.assertEquals(0L, defaultStats.getNumArticlesProcessed());
        Assert.assertNull(defaultStats.getDumpFileName());
    }

    @Test
    public void testProcessDatabaseStatistics() {
        long id = 12;
        boolean forceProcess = true;
        long numArticlesRead = 1000;
        long numArticlesProcessable = 800;
        long numArticlesProcessed = 500;
        String dumpFileName = "xxx.xml.bz2";
        long start = 1500;
        long end = 2000;
        IndexationEntity indexation = new IndexationEntity(id, forceProcess, numArticlesRead, numArticlesProcessable,
                numArticlesProcessed, dumpFileName, start, end);
        Mockito.when(indexationRepository.findByOrderByIdDesc(Mockito.any(Pageable.class)))
                .thenReturn(Collections.singletonList(indexation));

        DumpIndexation dbStats = dumpHandler.getProcessStatus();

        Assert.assertFalse(dbStats.isRunning());
        Assert.assertEquals(forceProcess, dbStats.isForceProcess());
        Assert.assertEquals(numArticlesRead, dbStats.getNumArticlesRead());
        Assert.assertEquals(numArticlesProcessable, dbStats.getNumArticlesProcessable());
        Assert.assertEquals(numArticlesProcessed, dbStats.getNumArticlesProcessed());
        Assert.assertEquals(dumpFileName, dbStats.getDumpFileName());
        Assert.assertEquals(start, dbStats.getStart());
        Assert.assertEquals(end, dbStats.getEnd().intValue());
    }

}
