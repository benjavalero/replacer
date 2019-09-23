package es.bvalero.replacer.dump;

import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Handler to parse a Wikipedia XML dump.
 */
@Slf4j
@Component
class DumpHandler extends DefaultHandler {

    private static final String TITLE_TAG = "title";
    private static final String NAMESPACE_TAG = "ns";
    private static final String ID_TAG = "id";
    private static final String TIMESTAMP_TAG = "timestamp";
    private static final String TEXT_TAG = "text";
    private static final String PAGE_TAG = "page";

    @Autowired
    private DumpArticleProcessor dumpArticleProcessor;

    // Get database replacements in batches to improve performance
    @Autowired
    private DumpArticleCache dumpArticleCache;

    // Current article values
    private StringBuilder currentChars = new StringBuilder(5000);
    private String currentTitle;
    private int currentNamespace;
    private int currentId;
    private String currentTimestamp;
    private String currentContent;

    // Status
    @Getter
    private boolean running = false;
    @Setter
    private Path latestDumpFile = null;
    @Setter
    private boolean forceProcess;
    private long numArticlesRead;
    private long numArticlesProcessable;
    private long numArticlesProcessed;
    private Instant startTime;
    private Instant endTime;

    @Override
    public void startDocument() {
        LOGGER.debug("START Handle dump document: {} - Force: {}", latestDumpFile, forceProcess);

        running = true;
        numArticlesRead = 0L;
        numArticlesProcessable = 0L;
        numArticlesProcessed = 0L;
        startTime = Instant.now();
        endTime = null;
    }

    @Override
    public void endDocument() {
        LOGGER.info("END handle dump document: {}", getProcessStatus());

        running = false;
        endTime = Instant.now();
        dumpArticleCache.clean();
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
                currentNamespace = Integer.parseInt(currentChars.toString());
                break;
            case ID_TAG:
                // ID appears several times (contributor, revision, etc). We care about the first one.
                if (currentId == 0) {
                    currentId = Integer.parseInt(currentChars.toString());
                }
                break;
            case TIMESTAMP_TAG:
                currentTimestamp = currentChars.toString();
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
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .id(currentId)
                .title(currentTitle)
                .namespace(WikipediaNamespace.valueOf(currentNamespace))
                .lastUpdate(WikipediaPage.parseWikipediaTimestamp(currentTimestamp))
                .content(currentContent)
                .queryTimestamp(WikipediaPage.formatWikipediaTimestamp(LocalDateTime.now()))
                .build();

        try {
            if (dumpArticleProcessor.isDumpArticleProcessable(dumpArticle)) {
                numArticlesProcessable++;
                boolean articleProcessed = processArticle(dumpArticle);
                if (articleProcessed) {
                    numArticlesProcessed++;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error processing dump page: {}", currentTitle, e);
        }
    }

    private boolean processArticle(WikipediaPage dumpArticle) {
        return dumpArticleProcessor.processArticle(dumpArticle);
    }

    DumpProcessStatus getProcessStatus() {
        return DumpProcessStatus.builder()
                .running(running)
                .forceProcess(forceProcess)
                .numArticlesRead(numArticlesRead)
                .numArticlesProcessable(numArticlesProcessable)
                .numArticlesProcessed(numArticlesProcessed)
                .dumpFileName(latestDumpFile == null ? "" : latestDumpFile.getFileName().toString())
                .start(startTime == null ? null : startTime.toEpochMilli())
                .end(endTime == null ? null : endTime.toEpochMilli())
                .build();
    }

}
