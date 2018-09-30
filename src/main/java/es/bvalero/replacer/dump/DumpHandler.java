package es.bvalero.replacer.dump;

import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Date;

/**
 * Handler to parse a Wikipedia XML dump.
 */
class DumpHandler extends DefaultHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DumpHandler.class);

    private StringBuilder currentChars = new StringBuilder();
    private Integer currentId;
    private String currentTitle;
    private WikipediaNamespace currentNamespace;
    private Date currentTimestamp;
    private String currentContent;

    // Options
    private DumpArticleProcessor dumpArticleProcessor;
    private boolean forceProcess;

    // Statistics
    private long numArticlesRead;
    private long numArticlesProcessed;
    private long startTime;
    private long endTime;

    DumpHandler(DumpArticleProcessor processor) {
        this(processor, false);
    }

    DumpHandler(DumpArticleProcessor processor, boolean forceProcess) {
        this.dumpArticleProcessor = processor;

        this.forceProcess = forceProcess;
        this.numArticlesRead = 0L;
        this.numArticlesProcessed = 0L;
        this.startTime = System.currentTimeMillis();
        this.endTime = 0L;
    }

    boolean isForceProcess() {
        return forceProcess;
    }

    long getNumArticlesRead() {
        return numArticlesRead;
    }

    long getNumArticlesProcessed() {
        return numArticlesProcessed;
    }

    long getStartTime() {
        return startTime;
    }

    long getEndTime() {
        return endTime;
    }

    @Override
    public void startDocument() {
    }

    @Override
    public void endDocument() {
        dumpArticleProcessor.finish();
        this.endTime = System.currentTimeMillis();
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
                numArticlesRead++;
                DumpArticle dumpArticle = new DumpArticle(currentId, currentTitle, currentNamespace, currentTimestamp, currentContent);

                try {
                    boolean articleProcessed = processArticle(dumpArticle);
                    if (articleProcessed) {
                        numArticlesProcessed++;
                    }
                } catch (Exception e) {
                    LOGGER.error("Error processing article: {}", currentTitle, e);
                }

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

    boolean processArticle(DumpArticle dumpArticle) {
        return dumpArticleProcessor.processArticle(dumpArticle, forceProcess);
    }

}
