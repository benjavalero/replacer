package es.bvalero.replacer.dump;

import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Date;

/**
 * Handler to parse the Wikipedia XML dump.
 */
@Component
class DumpHandler extends DefaultHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DumpHandler.class);

    private final StringBuilder currentChars = new StringBuilder();

    private Integer currentId;
    private String currentTitle;
    private WikipediaNamespace currentNamespace;
    private Date currentTimestamp;
    private String currentContent;

    private DumpArticle currentArticle;
    private int numProcessedItems;
    private boolean processOldArticles;

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
            case "title":
                currentTitle = this.currentChars.toString();
                break;
            case "ns":
                currentNamespace = WikipediaNamespace.valueOf(Integer.valueOf(this.currentChars.toString()));
                break;
            case "id":
                // ID appears twice. We care about the first one. It will be rest after processing the article.
                if (currentId == null) {
                    currentId = Integer.parseInt(this.currentChars.toString());
                }
                break;
            case "timestamp":
                currentTimestamp = WikipediaUtils.parseWikipediaDate(this.currentChars.toString());
                break;
            case "text":
                currentContent = this.currentChars.toString();
                break;
            case "page":
                currentArticle = new DumpArticle(
                        currentId, currentTitle, currentNamespace, currentTimestamp, currentContent);
                processArticle();
                // Reset current ID to avoid duplicates
                currentId = null;
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

    void setProcessOldArticles(boolean processOldArticles) {
        this.processOldArticles = processOldArticles;
    }

    private void processArticle() {
        try {
            dumpProcessor.processArticle(getCurrentArticle(), this.processOldArticles);
            this.numProcessedItems++;
        } catch (Exception e) {
            LOGGER.error("Error processing article: " + currentTitle, e);
        }
    }

}
