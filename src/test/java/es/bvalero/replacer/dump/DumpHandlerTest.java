package es.bvalero.replacer.dump;

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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;

public class DumpHandlerTest {

    @Mock
    private DumpProcessor dumpProcessor;

    @InjectMocks
    private DumpHandler dumpHandler;

    @Before
    public void setUp() {
        dumpHandler = new DumpHandler();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testParseDumpXmlFile() throws SAXException, ParserConfigurationException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        SAXParser saxParser = factory.newSAXParser();

        String dumpFilePath = getClass().getResource("/pages-articles.xml").getFile();
        InputStream xmlInput = new FileInputStream(dumpFilePath);
        saxParser.parse(xmlInput, dumpHandler);

        Assert.assertEquals(2, dumpHandler.getNumProcessedItems());
        Assert.assertEquals("Andorra", dumpHandler.getCurrentArticle().getTitle());
        Assert.assertEquals(Integer.valueOf(0), dumpHandler.getCurrentArticle().getNamespace());
        Assert.assertEquals("{{otros usos}}", dumpHandler.getCurrentArticle().getContent());

        GregorianCalendar cal = new GregorianCalendar(2016, 3, 17, 8, 55, 54);
        cal.setTimeZone(DumpHandler.TIME_ZONE);
        Assert.assertEquals(cal.getTime(), dumpHandler.getCurrentArticle().getTimestamp());

        Mockito.verify(dumpProcessor, Mockito.times(2))
                .processArticle(dumpHandler.getCurrentArticle());

        xmlInput.close();
    }

    @Test
    public void testParseWikipediaDateError() {
        Assert.assertNull(dumpHandler.parseWikipediaDate("xxxxx"));
    }

    @Test
    public void testProcessingError() throws SAXException, ParserConfigurationException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        SAXParser saxParser = factory.newSAXParser();

        Mockito.doThrow(Exception.class).when(dumpProcessor).processArticle(Mockito.any(DumpArticle.class));

        String dumpFilePath = getClass().getResource("/pages-articles.xml").getFile();
        InputStream xmlInput = new FileInputStream(dumpFilePath);
        saxParser.parse(xmlInput, dumpHandler);

        Mockito.verify(dumpProcessor, Mockito.times(2))
                .processArticle(dumpHandler.getCurrentArticle());
        Assert.assertEquals(0, dumpHandler.getNumProcessedItems());

        xmlInput.close();
    }

}
