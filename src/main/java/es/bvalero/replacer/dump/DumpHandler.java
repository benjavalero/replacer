package es.bvalero.replacer.dump;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Component
public class DumpHandler extends DefaultHandler {

    static final TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT");
    private static final Logger LOGGER = LoggerFactory.getLogger(DumpHandler.class);
    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN);
    private final StringBuilder currentChars = new StringBuilder();
    private Integer currentId;
    private String currentTitle;
    private Integer currentNamespace;
    private String currentText;
    private Date currentTimestamp;
    private int numItemsProcessed;

    @Autowired
    private DumpProcessor dumpProcessor;

    @Override
    public void startDocument() {
        this.numItemsProcessed = 0;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        this.currentChars.delete(0, this.currentChars.length());
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case "page":
                processArticle();
                this.numItemsProcessed++;
                setCurrentId(null);
                break;
            case "id":
                // ID appears twice. We care about the first one.
                if (getCurrentId() == null) {
                    setCurrentId(Integer.parseInt(this.currentChars.toString()));
                }
                break;
            case "title":
                setCurrentTitle(this.currentChars.toString());
                break;
            case "ns":
                setCurrentNamespace(Integer.parseInt(this.currentChars.toString()));
                break;
            case "text":
                setCurrentText(this.currentChars.toString());
                break;
            case "timestamp":
                setCurrentTimestamp(parseWikipediaDate(this.currentChars.toString()));
                break;
            default:
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        this.currentChars.append(ch, start, length);
    }

    private Integer getCurrentId() {
        return currentId;
    }

    private void setCurrentId(Integer currentId) {
        this.currentId = currentId;
    }

    String getCurrentTitle() {
        return currentTitle;
    }

    private void setCurrentTitle(String currentTitle) {
        this.currentTitle = currentTitle;
    }

    Integer getCurrentNamespace() {
        return currentNamespace;
    }

    void setCurrentNamespace(Integer currentNamespace) {
        this.currentNamespace = currentNamespace;
    }

    Date getCurrentTimestamp() {
        return currentTimestamp;
    }

    private void setCurrentTimestamp(Date currentTimestamp) {
        this.currentTimestamp = currentTimestamp;
    }

    String getCurrentText() {
        return currentText;
    }

    void setCurrentText(String currentText) {
        this.currentText = currentText;
    }

    int getNumItemsProcessed() {
        return numItemsProcessed;
    }

    Date parseWikipediaDate(String dateStr) {
        Date wikiDate = null;
        try {
            DATE_FORMAT.setTimeZone(TIME_ZONE);
            wikiDate = DATE_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            LOGGER.error("Error parsing Wikipedia date: {}", dateStr, e);
        }
        return wikiDate;
    }

    private void processArticle() {
        try {
            dumpProcessor.processArticle(getCurrentId(), getCurrentText(), getCurrentNamespace(), getCurrentTitle(), getCurrentTimestamp());
        } catch (Exception e) {
            LOGGER.error("Error processing article: " + getCurrentTitle(), e);
        }
    }

}
