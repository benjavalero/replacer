package es.bvalero.replacer.dump;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaDateUtils;
import java.time.Instant;
import lombok.Getter;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handler to parse a Wikipedia XML dump with SAX.
 * It will be instantiated for each parse job.
 */
class DumpSaxHandler extends DefaultHandler {

    private static final String TITLE_TAG = "title";
    private static final String NAMESPACE_TAG = "ns";
    private static final String ID_TAG = "id";
    private static final String TIMESTAMP_TAG = "timestamp";
    private static final String TEXT_TAG = "text";
    private static final String PAGE_TAG = "page";

    // Dump properties
    @Getter
    private final WikipediaLanguage lang;

    private final DumpPageProcessor dumpPageProcessor;

    // Current page values
    private final StringBuilder currentChars = new StringBuilder(5000);
    private String currentTitle;
    private int currentNamespace;
    private int currentId;
    private String currentTimestamp;
    private String currentContent;

    // Indexing status
    @Getter
    private boolean running = false;

    @Getter
    private long numPagesRead = 0L;

    @Getter
    private long numPagesProcessed = 0L;

    @Getter
    private Long start = null;

    @Getter
    private Long end = null;

    DumpSaxHandler(WikipediaLanguage lang, DumpPageProcessor processor) {
        this.lang = lang;
        this.dumpPageProcessor = processor;
    }

    @Override
    public void startDocument() {
        // Reset indexing status
        this.running = true;
        this.start = Instant.now().toEpochMilli();
    }

    @Override
    public void endDocument() {
        // Finish indexing status
        this.running = false;
        this.end = Instant.now().toEpochMilli();

        this.dumpPageProcessor.finish(lang);
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
                // ID appears several times (contributor, revision, etc). We care about the first one.
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
            case PAGE_TAG:
                processPage();

                // Reset current ID and Content to avoid duplicates
                this.currentId = 0;
                this.currentContent = null;
                break;
            default:
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        this.currentChars.append(ch, start, length);
    }

    private void processPage() {
        DumpPage dumpPage = DumpPage
            .builder()
            .lang(this.lang)
            .id(this.currentId)
            .namespace(WikipediaNamespace.valueOf(this.currentNamespace))
            .title(this.currentTitle)
            .content(this.currentContent)
            .lastUpdate(WikipediaDateUtils.parseWikipediaTimestamp(this.currentTimestamp))
            .build();

        DumpPageProcessorResult result = dumpPageProcessor.process(dumpPage);
        if (!DumpPageProcessorResult.PAGE_NOT_PROCESSABLE.equals(result)) {
            this.incrementNumPagesRead();
            if (DumpPageProcessorResult.PAGE_PROCESSED.equals(result)) {
                this.incrementNumPagesProcessed();
            }
        }
    }

    private void incrementNumPagesRead() {
        this.numPagesRead++;
    }

    private void incrementNumPagesProcessed() {
        this.numPagesProcessed++;
    }
}
