package es.bvalero.replacer.dump;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.index.IndexablePage;
import es.bvalero.replacer.index.PageIndexApi;
import es.bvalero.replacer.index.PageIndexStatus;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    private final PageIndexApi pageIndexApi;

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

    DumpSaxHandler(WikipediaLanguage lang, PageIndexApi indexer) {
        this.lang = lang;
        this.pageIndexApi = indexer;
    }

    @Override
    public void startDocument() {
        // Reset indexing status
        this.running = true;
        this.start = LocalDateTime.now(ZoneId.systemDefault());
    }

    @Override
    public void endDocument() {
        // Finish indexing status
        this.running = false;
        this.end = LocalDateTime.now(ZoneId.systemDefault());

        this.pageIndexApi.finish();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        this.currentChars.delete(0, this.currentChars.length());
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        switch (qName) {
            case TITLE_TAG -> this.currentTitle = this.currentChars.toString();
            case NAMESPACE_TAG -> this.currentNamespace = Integer.parseInt(this.currentChars.toString());
            case ID_TAG -> {
                // ID appears several times: contributor, revision, etc. We care about the first one.
                if (this.currentId == 0) {
                    this.currentId = Integer.parseInt(this.currentChars.toString());
                }
            }
            case TIMESTAMP_TAG -> this.currentTimestamp = this.currentChars.toString();
            case TEXT_TAG -> {
                // Text appears several times (contributor, revision, etc). We care about the first one.
                if (this.currentContent == null) {
                    this.currentContent = this.currentChars.toString();
                }
            }
            case REDIRECT_TAG -> this.currentRedirect = true; // If the tag appears it means it is a redirection page
            case PAGE_TAG -> {
                try {
                    indexPage();
                } catch (Exception e) {
                    LOGGER.error("Error parsing dump page: {} - {} - {}", lang, this.currentId, this.currentTitle, e);
                }

                // Reset current ID and Content to avoid duplicates
                this.currentId = 0;
                this.currentContent = null;
                this.currentRedirect = false;
            }
            default -> {}
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        this.currentChars.append(ch, start, length);
    }

    private void indexPage() {
        final IndexablePage page = IndexablePage.builder()
            .pageKey(PageKey.of(this.lang, this.currentId))
            .namespace(this.currentNamespace)
            .title(this.currentTitle)
            .content(this.currentContent)
            .lastUpdate(this.currentTimestamp)
            .redirect(this.currentRedirect)
            .build();

        final PageIndexStatus result = pageIndexApi.indexPage(page).getStatus();
        switch (result) {
            case PAGE_NOT_INDEXABLE -> {}
            case PAGE_NOT_INDEXED -> this.incrementNumPagesRead();
            case PAGE_INDEXED -> {
                this.incrementNumPagesRead();
                this.incrementNumPagesIndexed();
            }
        }
    }

    private void incrementNumPagesRead() {
        this.numPagesRead++;
    }

    private void incrementNumPagesIndexed() {
        this.numPagesIndexed++;
    }
}
