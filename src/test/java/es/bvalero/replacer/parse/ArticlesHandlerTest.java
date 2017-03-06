package es.bvalero.replacer.parse;

import org.junit.After;
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

public class ArticlesHandlerTest {

    private SAXParser saxParser;
    private InputStream xmlInput;
    private ArticlesHandler handler;

    @Before
    public void setUp() {
        String xmlPath = getClass().getResource("/pages-articles.xml").getFile();
        File articlesFile = new File(xmlPath);
        SAXParserFactory factory = SAXParserFactory.newInstance();
        handler = new ArticlesHandler() {
            @Override
            void processArticle() {
            }
        };
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            saxParser = factory.newSAXParser();
            xmlInput = new FileInputStream(articlesFile);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        try {
            xmlInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetCurrentTitle() {
        try {
            saxParser.parse(xmlInput, handler);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }

        Assert.assertEquals("Andorra", handler.getCurrentTitle());
    }

    @Test
    public void testGetCurrentNamespace() {
        try {
            saxParser.parse(xmlInput, handler);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(Integer.valueOf(0), handler.getCurrentNamespace());
    }

    @Test
    public void testGetCurrentTimestamp() {
        try {
            saxParser.parse(xmlInput, handler);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }

        GregorianCalendar cal = new GregorianCalendar(2016, 3, 17, 8, 55, 54);
        cal.setTimeZone(ArticlesHandler.TIME_ZONE);
        Assert.assertEquals(cal.getTime(), handler.getCurrentTimestamp());
    }

    @Test
    public void testParseBadWikipediaDate() {
        Assert.assertNull(handler.parseWikipediaDate("xxxxx"));
    }

    @Test
    public void testGetCurrentText() {
        try {
            saxParser.parse(xmlInput, handler);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }

        Assert.assertEquals("{{otros usos}}", handler.getCurrentText());
    }

}
