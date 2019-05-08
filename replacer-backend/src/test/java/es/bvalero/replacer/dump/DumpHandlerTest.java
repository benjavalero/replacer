package es.bvalero.replacer.dump;

import es.bvalero.replacer.wikipedia.WikipediaPage;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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

public class DumpHandlerTest {

    @Mock
    private DumpArticleProcessor dumpArticleProcessor;

    @InjectMocks
    private DumpHandler dumpHandler;

    @Before
    public void setUp() {
        dumpHandler = new DumpHandler();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHandleDumpFile() throws URISyntaxException, DumpException {
        Path dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        Mockito.when(dumpArticleProcessor.processArticle(Mockito.any(WikipediaPage.class), Mockito.anyBoolean()))
                .thenReturn(Boolean.TRUE);

        try (InputStream xmlInput = new BZip2CompressorInputStream(Files.newInputStream(dumpFile))) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            SAXParser saxParser = factory.newSAXParser();

            // Parse with the Dump Handler
            saxParser.parse(xmlInput, dumpHandler);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new DumpException("", e);
        }

        // Check number of articles read and processed (we have mocked the result from the processor)
        DumpProcessStatus status = dumpHandler.getProcessStatus();
        Assert.assertEquals(4L, status.getNumArticlesRead());
        Assert.assertEquals(4L, status.getNumArticlesProcessed());
    }

    @Test
    public void testHandDumpFileWithException() throws URISyntaxException, DumpException {
        Path dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        Mockito.when(dumpArticleProcessor.processArticle(Mockito.any(WikipediaPage.class)))
                .thenThrow(Mockito.mock(NullPointerException.class));

        try (InputStream xmlInput = new BZip2CompressorInputStream(Files.newInputStream(dumpFile))) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            SAXParser saxParser = factory.newSAXParser();

            // Parse with the Dump Handler
            saxParser.parse(xmlInput, dumpHandler);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new DumpException("", e);
        }

        // Check number of articles read and processed (we have mocked the result from the processor)
        DumpProcessStatus status = dumpHandler.getProcessStatus();
        Assert.assertEquals(4L, status.getNumArticlesRead());
        Assert.assertEquals(0L, status.getNumArticlesProcessed());
    }

    @Test
    public void testProcessDefaultStatistics() {
        // Default statistics
        DumpProcessStatus defaultStats = dumpHandler.getProcessStatus();
        Assert.assertFalse(defaultStats.isRunning());
        Assert.assertFalse(defaultStats.isForceProcess());
        Assert.assertEquals(0L, defaultStats.getNumArticlesRead());
        Assert.assertEquals(0L, defaultStats.getNumArticlesProcessed());
        Assert.assertEquals("-", defaultStats.getDumpFileName());
        Assert.assertEquals(0L, defaultStats.getAverage());
        Assert.assertEquals("0:00:00:00", defaultStats.getTime());
        Assert.assertNull(defaultStats.getProgress());
    }

}
