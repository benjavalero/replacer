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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;

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
        File dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI()).toFile();
        Mockito.when(dumpArticleProcessor.processArticle(Mockito.any(DumpArticle.class), Mockito.anyBoolean()))
                .thenReturn(Boolean.TRUE);

        try (InputStream xmlInput = new BZip2CompressorInputStream(new FileInputStream(dumpFile))) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            SAXParser saxParser = factory.newSAXParser();

            // Parse with the Dump Handler
            saxParser.parse(xmlInput, dumpHandler);
        } catch (IOException e) {
            throw new DumpException("Dump file not valid: " + dumpFile, e);
        } catch (ParserConfigurationException | SAXException e) {
            throw new DumpException("SAX Error parsing dump file: " + dumpFile, e);
        }

        // Check number of articles read and processed (we have mocked the result from the processor)
        Assert.assertEquals(4, dumpHandler.getNumArticlesRead());
        Assert.assertEquals(4, dumpHandler.getNumArticlesProcessed());
    }

    @Test
    public void testHandDumpFileWithException() throws URISyntaxException, DumpException {
        File dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI()).toFile();
        Mockito.when(dumpArticleProcessor.processArticle(Mockito.any(DumpArticle.class)))
                .thenThrow(Mockito.mock(NullPointerException.class));

        try (InputStream xmlInput = new BZip2CompressorInputStream(new FileInputStream(dumpFile))) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            SAXParser saxParser = factory.newSAXParser();

            // Parse with the Dump Handler
            saxParser.parse(xmlInput, dumpHandler);
        } catch (IOException e) {
            throw new DumpException("Dump file not valid: " + dumpFile, e);
        } catch (ParserConfigurationException | SAXException e) {
            throw new DumpException("SAX Error parsing dump file: " + dumpFile, e);
        }

        // Check number of articles read and processed (we have mocked the result from the processor)
        Assert.assertEquals(4, dumpHandler.getNumArticlesRead());
        Assert.assertEquals(0, dumpHandler.getNumArticlesProcessed());
    }

}
