package es.bvalero.replacer.dump;

import es.bvalero.replacer.parse.ArticleHandler;
import es.bvalero.replacer.utils.RegExUtils;
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
import java.util.List;
import java.util.TimeZone;

@Component
public class DumpHandler extends DefaultHandler {

    static final TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT");
    static final Integer NAMESPACE_ARTICLE = 0;
    static final Integer NAMESPACE_ANNEX = 104;
    private static final Logger LOGGER = LoggerFactory.getLogger(DumpHandler.class);
    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN);
    private final StringBuilder currentChars = new StringBuilder();
    private String currentTitle;
    private Integer currentNamespace;
    private String currentText;
    private Date currentTimestamp;
    private int numItemsProcessed;

    @Autowired
    private List<ArticleHandler> articleHandlers;

    @Override
    public void startDocument() {
        numItemsProcessed = 0;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentChars.delete(0, currentChars.length());
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case "page":
                processArticle();
                this.numItemsProcessed++;
                break;
            case "title":
                setCurrentTitle(currentChars.toString());
                break;
            case "ns":
                currentNamespace = Integer.parseInt(currentChars.toString());
                break;
            case "text":
                setCurrentText(currentChars.toString());
                break;
            case "timestamp":
                setCurrentTimestamp(parseWikipediaDate(currentChars.toString()));
                break;
            default:
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        currentChars.append(ch, start, length);
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

    void processArticle() {
        if (hasToBeProcessed()) {
            for (ArticleHandler articleHandler : articleHandlers) {
                articleHandler.processArticle(getCurrentText(), getCurrentTitle(), getCurrentTimestamp());
            }
        }
    }

    boolean hasToBeProcessed() {
        // Only process articles or annexes
        // Skip redirection articles
        return (isArticle() || isAnnex()) && !isRedirectionArticle();
    }

    boolean isArticle() {
        return NAMESPACE_ARTICLE.equals(getCurrentNamespace());
    }

    boolean isAnnex() {
        return NAMESPACE_ANNEX.equals(getCurrentNamespace());
    }

    private boolean isRedirectionArticle() {
        return RegExUtils.isRedirectionArticle(getCurrentText());
    }

}
