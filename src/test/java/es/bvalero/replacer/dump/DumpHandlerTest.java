package es.bvalero.replacer.dump;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeParseException;

public class DumpHandlerTest {

    @Mock
    private DumpArticleProcessor dumpArticleProcessor;

    private DumpHandler dumpHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        dumpHandler = new DumpHandler(dumpArticleProcessor);
    }

    @Test
    public void testHandleDumpFile() throws URISyntaxException, DumpException {
        Path dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        Mockito.when(dumpArticleProcessor.processArticle(Mockito.any(DumpArticle.class), Mockito.anyBoolean()))
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
        Assert.assertEquals(4L, dumpHandler.getNumArticlesRead());
        Assert.assertEquals(4L, dumpHandler.getNumArticlesProcessed());
    }

    @Test
    public void testHandDumpFileWithException() throws URISyntaxException, DumpException {
        Path dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        Mockito.when(dumpArticleProcessor.processArticle(Mockito.any(DumpArticle.class)))
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
        Assert.assertEquals(4L, dumpHandler.getNumArticlesRead());
        Assert.assertEquals(0L, dumpHandler.getNumArticlesProcessed());
    }


    @Test
    public void testParseWikipediaDate() {
        LocalDateTime expected = LocalDateTime.of(2018, Month.AUGUST, 31, 5, 17, 28);
        Assert.assertEquals(expected, DumpHandler.parseWikipediaDate("2018-08-31T05:17:28Z"));
    }

    @Test(expected = DateTimeParseException.class)
    public void testParseWikipediaDateBadFormat() {
        DumpHandler.parseWikipediaDate("xxx");
    }

}
