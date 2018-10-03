package es.bvalero.replacer.dump;

import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaUtils;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.time.LocalDateTime;

/**
 * Handler to parse a Wikipedia XML dump.
 */
class DumpHandler extends DefaultHandler {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(DumpHandler.class);

    private final DumpArticleProcessor dumpArticleProcessor;
    private final boolean forceProcess;
    private final long startTime = System.currentTimeMillis();

    private final StringBuilder currentChars = new StringBuilder(5000);
    private Integer currentId;
    private String currentTitle;
    private WikipediaNamespace currentNamespace;
    private LocalDateTime currentTimestamp;
    private String currentContent;

    // Statistics
    private long numArticlesRead;
    private long numArticlesProcessed;
    private long endTime;

    DumpHandler(DumpArticleProcessor processor) {
        this(processor, false);
    }

    DumpHandler(DumpArticleProcessor processor, boolean forceProcess) {
        dumpArticleProcessor = processor;
        this.forceProcess = forceProcess;
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
        endTime = System.currentTimeMillis();
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
                currentNamespace = WikipediaNamespace.valueOf(Integer.parseInt(currentChars.toString()));
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
                DumpArticle dumpArticle = DumpArticle.builder()
                        .setId(currentId)
                        .setTitle(currentTitle)
                        .setNamespace(currentNamespace)
                        .setTimestamp(currentTimestamp)
                        .setContent(currentContent)
                        .build();

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
