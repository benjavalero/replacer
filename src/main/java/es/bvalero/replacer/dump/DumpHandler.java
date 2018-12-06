package es.bvalero.replacer.dump;

import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Handler to parse a Wikipedia XML dump.
 */
class DumpHandler extends DefaultHandler {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(DumpHandler.class);
    private static final String TITLE_TAG = "title";
    private static final String NAMESPACE_TAG = "ns";
    private static final String ID_TAG = "id";
    private static final String TIMESTAMP_TAG = "timestamp";
    private static final String TEXT_TAG = "text";
    private static final String PAGE_TAG = "page";
    private static final String WIKIPEDIA_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private final DumpArticleProcessor dumpArticleProcessor;
    private final boolean forceProcess;

    private final StringBuilder currentChars = new StringBuilder(5000);
    private int currentId;
    private String currentTitle;
    private WikipediaNamespace currentNamespace;
    private LocalDateTime currentTimestamp;
    private String currentContent;

    // Statistics
    private long numArticlesRead;
    private long numArticlesProcessed;
    private Instant startTime;
    private Instant endTime;

    DumpHandler(DumpArticleProcessor processor) {
        this(processor, false);
    }

    DumpHandler(DumpArticleProcessor processor, boolean forceProcess) {
        dumpArticleProcessor = processor;
        this.forceProcess = forceProcess;
    }

    static LocalDateTime parseWikipediaDate(CharSequence dateStr) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(WIKIPEDIA_DATE_PATTERN);
        return LocalDateTime.from(dateFormat.parse(dateStr));
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

    Instant getStartTime() {
        return startTime;
    }

    Instant getEndTime() {
        return endTime;
    }

    @Override
    public void startDocument() {
        numArticlesRead = 0L;
        numArticlesProcessed = 0L;
        startTime = Instant.now();
    }

    @Override
    public void endDocument() {
        dumpArticleProcessor.finish();
        endTime = Instant.now();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        currentChars.delete(0, currentChars.length());
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        switch (qName) {
            case TITLE_TAG:
                currentTitle = currentChars.toString();
                break;
            case NAMESPACE_TAG:
                currentNamespace = WikipediaNamespace.valueOf(Integer.parseInt(currentChars.toString()));
                break;
            case ID_TAG:
                // ID appears several times (contributor, revision, etc). We care about the first one.
                if (currentId == 0) {
                    currentId = Integer.parseInt(currentChars.toString());
                }
                break;
            case TIMESTAMP_TAG:
                currentTimestamp = parseWikipediaDate(currentChars.toString());
                break;
            case TEXT_TAG:
                currentContent = currentChars.toString();
                break;
            case PAGE_TAG:
                processPage();

                // Reset current ID to avoid duplicates
                currentId = 0;
                break;
            default:
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        currentChars.append(ch, start, length);
    }

    private void processPage() {
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
    }

    boolean processArticle(DumpArticle dumpArticle) {
        return dumpArticleProcessor.processArticle(dumpArticle, forceProcess);
    }

}
