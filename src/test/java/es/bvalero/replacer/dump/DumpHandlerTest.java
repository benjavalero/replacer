package es.bvalero.replacer.dump;

import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

    private DumpHandler dumpHandler;

    @Before
    public void setUp() {
        dumpHandler = new DumpHandler() {
            @Override
            void processArticle(DumpArticle article) {
                // Do nothing
            }
        };
    }

    @Test
    public void testParseDumpXmlFile() throws SAXException, ParserConfigurationException, IOException {
        String dumpFilePath = getClass().getResource("/pages-articles.xml").getFile();

        try (InputStream xmlInput = new FileInputStream(dumpFilePath)) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            SAXParser saxParser = factory.newSAXParser();

            saxParser.parse(xmlInput, dumpHandler);
        } finally {
            dumpHandler.getDumpStatus().finish();
        }

        // Test that all articles are processed
        Assert.assertEquals(3, dumpHandler.getDumpStatus().getPagesCount());

        // Test values of the last processed article
        Assert.assertEquals(Integer.valueOf(7), dumpHandler.getCurrentArticle().getId());
        Assert.assertEquals("Andorra", dumpHandler.getCurrentArticle().getTitle());
        Assert.assertEquals(WikipediaNamespace.ARTICLE, dumpHandler.getCurrentArticle().getNamespace());
        Assert.assertEquals("{{otros usos}}", dumpHandler.getCurrentArticle().getContent());

        GregorianCalendar cal = new GregorianCalendar(2016, 3, 17, 8, 55, 54);
        cal.setTimeZone(WikipediaUtils.TIME_ZONE);
        Assert.assertEquals(cal.getTime(), dumpHandler.getCurrentArticle().getTimestamp());
    }

    @Test
    public void testProcessableArticles() {
        Assert.assertTrue(new DumpArticle(1, "", WikipediaNamespace.ARTICLE, null, "").isProcessable());
        Assert.assertFalse(new DumpArticle(2, "", WikipediaNamespace.USER, null, "").isProcessable());
        Assert.assertTrue(new DumpArticle(3, "", WikipediaNamespace.ANNEX, null, "").isProcessable());
        Assert.assertFalse(new DumpArticle(4, "", WikipediaNamespace.ARTICLE, null, "#REDIRECT xxx")
                .isProcessable());
    }

}
