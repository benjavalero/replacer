package es.bvalero.replacer.parse;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class ArticlesParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArticlesParser.class);

    public boolean parse(String articlesPath, ArticlesHandler handler) {
        boolean success = false;
        File articlesFile = new File(articlesPath);
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            SAXParser saxParser = factory.newSAXParser();
            InputStream xmlInput = new BZip2CompressorInputStream(new FileInputStream(articlesFile));
            saxParser.parse(xmlInput, handler);
            xmlInput.close();
            success = true;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.error("Error parsing articles in: {}", articlesPath, e);
        }

        return success;
    }

}
