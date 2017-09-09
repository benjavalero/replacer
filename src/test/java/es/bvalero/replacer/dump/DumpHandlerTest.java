package es.bvalero.replacer.dump;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;

public class DumpHandlerTest {

    private DumpHandler dumpHandler;

    @Before
    public void setUp() {
        dumpHandler = new DumpHandler() {
            @Override
            void processArticle() {
            }
        };
    }

    @Test
    public void testParseDumpXmlFile()
            throws SAXException, ParserConfigurationException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        SAXParser saxParser = factory.newSAXParser();

        String dumpFilePath = getClass().getResource("/pages-articles.xml").getFile();
        File dumpFile = new File(dumpFilePath);
        InputStream xmlInput = new FileInputStream(dumpFile);
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

    @Test
    public void testIsArticle() {
        dumpHandler.setCurrentNamespace(DumpHandler.NAMESPACE_ARTICLE);
        Assert.assertTrue(dumpHandler.isArticle());

        dumpHandler.setCurrentNamespace(DumpHandler.NAMESPACE_ANNEX);
        Assert.assertFalse(dumpHandler.isArticle());
    }

    @Test
    public void testIsAnnex() {
        dumpHandler.setCurrentNamespace(DumpHandler.NAMESPACE_ANNEX);
        Assert.assertTrue(dumpHandler.isAnnex());

        dumpHandler.setCurrentNamespace(DumpHandler.NAMESPACE_ARTICLE);
        Assert.assertFalse(dumpHandler.isAnnex());
    }

    @Test
    public void testHasToBeProcessed() {
        dumpHandler.setCurrentText("");
        dumpHandler.setCurrentNamespace(DumpHandler.NAMESPACE_ANNEX);
        Assert.assertTrue(dumpHandler.hasToBeProcessed());

        dumpHandler.setCurrentNamespace(DumpHandler.NAMESPACE_ARTICLE);
        Assert.assertTrue(dumpHandler.hasToBeProcessed());

        dumpHandler.setCurrentText("#REDIRECCIÓN [[A]]");
        Assert.assertFalse(dumpHandler.hasToBeProcessed());
    }

}
