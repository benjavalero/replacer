package es.bvalero.replacer.dump;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.index.PageIndexBatchService;
import es.bvalero.replacer.page.index.PageIndexStatus;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handler to parse a Wikipedia XML dump with SAX.
 * It will be instantiated for each parse job.
 */
@Slf4j
class DumpSaxHandler extends DefaultHandler {

    private static final String TITLE_TAG = "title";
    private static final String NAMESPACE_TAG = "ns";
    private static final String ID_TAG = "id";
    private static final String TIMESTAMP_TAG = "timestamp";
    private static final String TEXT_TAG = "text";
    private static final String PAGE_TAG = "page";
    private static final String REDIRECT_TAG = "redirect";

    // Dump properties
    @Getter
    private final WikipediaLanguage lang;

    private final PageIndexBatchService pageIndexService;

    // Current page values
    private final StringBuilder currentChars = new StringBuilder(5000);
    private String currentTitle;
    private int currentNamespace;
    private int currentId;
    private String currentTimestamp;
    private String currentContent;
    private boolean currentRedirect;

    // Indexing status
    @Getter
    private boolean running = false;

    @Getter
    private int numPagesRead = 0;

    @Getter
    private int numPagesIndexed = 0;

    @Getter
    private LocalDateTime start = null;

    @Getter
    private LocalDateTime end = null;

    DumpSaxHandler(WikipediaLanguage lang, PageIndexBatchService indexer) {
        this.lang = lang;
        this.pageIndexService = indexer;
    }

    @Override
    public void startDocument() {
        // Reset indexing status
        this.running = true;
        this.start = LocalDateTime.now();
    }

    @Override
    public void endDocument() {
        // Finish indexing status
        this.running = false;
        this.end = LocalDateTime.now();

        this.pageIndexService.finish();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        this.currentChars.delete(0, this.currentChars.length());
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        switch (qName) {
            case TITLE_TAG:
                this.currentTitle = this.currentChars.toString();
                break;
            case NAMESPACE_TAG:
                this.currentNamespace = Integer.parseInt(this.currentChars.toString());
                break;
            case ID_TAG:
                // ID appears several times: contributor, revision, etc. We care about the first one.
                if (this.currentId == 0) {
                    this.currentId = Integer.parseInt(this.currentChars.toString());
                }
                break;
            case TIMESTAMP_TAG:
                this.currentTimestamp = this.currentChars.toString();
                break;
            case TEXT_TAG:
                // Text appears several times (contributor, revision, etc). We care about the first one.
                if (this.currentContent == null) {
                    this.currentContent = this.currentChars.toString();
                }
                break;
            case REDIRECT_TAG:
                // If the tag appears it means it is a redirection page
                this.currentRedirect = true;
                break;
            case PAGE_TAG:
                try {
                    indexPage();
                } catch (Exception e) {
                    LOGGER.error("Error parsing dump page: {} - {} - {}", lang, this.currentId, this.currentTitle, e);
                }

                // Reset current ID and Content to avoid duplicates
                this.currentId = 0;
                this.currentContent = null;
                this.currentRedirect = false;
                break;
            default:
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        this.currentChars.append(ch, start, length);
    }

    private void indexPage() {
        final WikipediaTimestamp timeStamp = WikipediaTimestamp.of(this.currentTimestamp);
        final WikipediaPage page = WikipediaPage.builder()
            .pageKey(PageKey.of(this.lang, this.currentId))
            .namespace(WikipediaNamespace.valueOf(this.currentNamespace))
            .title(this.currentTitle)
            .content(this.currentContent)
            .lastUpdate(timeStamp)
            .queryTimestamp(timeStamp)
            .redirect(this.currentRedirect)
            .build();

        final PageIndexStatus result = pageIndexService.indexPage(page).getStatus();
        switch (result) {
            case PAGE_NOT_INDEXABLE:
                break;
            case PAGE_NOT_INDEXED:
                this.incrementNumPagesRead();
                break;
            case PAGE_INDEXED:
                this.incrementNumPagesRead();
                this.incrementNumPagesIndexed();
                break;
        }
    }

    private void incrementNumPagesRead() {
        this.numPagesRead++;
    }

    private void incrementNumPagesIndexed() {
        this.numPagesIndexed++;
    }
}
