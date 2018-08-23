package es.bvalero.replacer.dump;

import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Date;

/**
 * Abstract handler to parse a Wikipedia XML dump. It keeps track of the number of articles processed.
 */
abstract class DumpHandler extends DefaultHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DumpHandler.class);

    private final StringBuilder currentChars = new StringBuilder();

    private Integer currentId;
    private String currentTitle;
    private WikipediaNamespace currentNamespace;
    private Date currentTimestamp;
    private String currentContent;

    private DumpArticle currentArticle;
    private int numProcessedItems;

    public void startDocument() {
        numProcessedItems = 0;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        currentChars.delete(0, currentChars.length());
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        switch (qName) {
            case "title":
                currentTitle = currentChars.toString();
                break;
            case "ns":
                currentNamespace = WikipediaNamespace.valueOf(Integer.valueOf(currentChars.toString()));
                break;
            case "id":
                // ID appears several times (contributor, revision, etc). We care about the first one.
                if (currentId == null) {
                    currentId = Integer.parseInt(currentChars.toString());
                }
                break;
            case "timestamp":
                currentTimestamp = WikipediaUtils.parseWikipediaDate(currentChars.toString());
                break;
            case "text":
                currentContent = currentChars.toString();
                break;
            case "page":
                currentArticle = new DumpArticle(
                        currentId, currentTitle, currentNamespace, currentTimestamp, currentContent);
                process();
                // Reset current ID to avoid duplicates
                currentId = null;
                break;
            default:
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        currentChars.append(ch, start, length);
    }

    DumpArticle getCurrentArticle() {
        return currentArticle;
    }

    int getNumProcessedItems() {
        return numProcessedItems;
    }

    abstract void processArticle(DumpArticle article);

    private void process() {
        try {
            // Check if it is really needed to process the article
            // in case it is not an article/annex or it is a redirection
            if (currentArticle.isProcessable()) {
                processArticle(currentArticle);
                numProcessedItems++;
            }
        } catch (Exception e) {
            LOGGER.error("Error processing article: {}", currentTitle, e);
        }
    }

}
