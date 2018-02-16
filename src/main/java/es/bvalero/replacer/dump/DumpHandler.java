package es.bvalero.replacer.dump;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Handler to parse the Wikipedia XML dump.
 */
@Component
public class DumpHandler extends DefaultHandler {

    static final TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT");
    private static final Logger LOGGER = LoggerFactory.getLogger(DumpHandler.class);
    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);

    private final StringBuilder currentChars = new StringBuilder();

    private DumpArticle currentArticle = new DumpArticle();
    private int numProcessedItems;

    @Autowired
    private DumpProcessor dumpProcessor;

    @Override
    public void startDocument() {
        this.numProcessedItems = 0;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        this.currentChars.delete(0, this.currentChars.length());
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        switch (qName) {
            case "page":
                processArticle();
                break;
            case "id":
                // ID appears twice. We care about the first one.
                if (getCurrentArticle().getId() == null) {
                    getCurrentArticle().setId(Integer.parseInt(this.currentChars.toString()));
                }
                break;
            case "title":
                getCurrentArticle().setTitle(this.currentChars.toString());
                break;
            case "ns":
                getCurrentArticle().setNamespace(Integer.parseInt(this.currentChars.toString()));
                break;
            case "text":
                getCurrentArticle().setContent(this.currentChars.toString());
                break;
            case "timestamp":
                getCurrentArticle().setTimestamp(parseWikipediaDate(this.currentChars.toString()));
                break;
            default:
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        this.currentChars.append(ch, start, length);
    }

    DumpArticle getCurrentArticle() {
        return currentArticle;
    }

    int getNumProcessedItems() {
        return numProcessedItems;
    }

    Date parseWikipediaDate(String dateStr) {
        Date wikiDate = null;
        try {
            dateFormat.setTimeZone(TIME_ZONE);
            wikiDate = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            LOGGER.error("Error parsing Wikipedia date: {}", dateStr, e);
        }
        return wikiDate;
    }

    private void processArticle() {
        try {
            dumpProcessor.processArticle(getCurrentArticle());
            this.numProcessedItems++;
        } catch (Exception e) {
            LOGGER.error("Error processing article: " + getCurrentArticle().getTitle(), e);
        }
    }

}
