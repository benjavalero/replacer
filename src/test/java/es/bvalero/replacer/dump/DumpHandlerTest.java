package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.ArticleService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
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

    @Spy
    private ArticleService articleService;

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
    public void testParseDumpXmlFile()
            throws SAXException, ParserConfigurationException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        SAXParser saxParser = factory.newSAXParser();

        String dumpFilePath = getClass().getResource("/pages-articles.xml").getFile();
        InputStream xmlInput = new FileInputStream(dumpFilePath);
        saxParser.parse(xmlInput, dumpHandler);

        Assert.assertEquals(1, dumpHandler.getNumItemsProcessed());
        Assert.assertEquals("Andorra", dumpHandler.getCurrentTitle());
        Assert.assertEquals(Integer.valueOf(0), dumpHandler.getCurrentNamespace());
        Assert.assertEquals("{{otros usos}}", dumpHandler.getCurrentText());

        GregorianCalendar cal = new GregorianCalendar(2016, 3, 17, 8, 55, 54);
        cal.setTimeZone(DumpHandler.TIME_ZONE);
        Assert.assertEquals(cal.getTime(), dumpHandler.getCurrentTimestamp());

        xmlInput.close();
    }

    @Test
    public void testParseWikipediaDateError() {
        Assert.assertNull(dumpHandler.parseWikipediaDate("xxxxx"));
    }

}
