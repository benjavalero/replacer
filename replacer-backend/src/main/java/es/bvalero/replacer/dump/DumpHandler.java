package es.bvalero.replacer.dump;

import es.bvalero.replacer.common.DateUtils;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.common.WikipediaNamespace;
import es.bvalero.replacer.replacement.IndexablePage;
import es.bvalero.replacer.replacement.ReplacementEntity;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handler to parse a Wikipedia XML dump with SAX.
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
    private DumpPageProcessor dumpPageProcessor;

    @Autowired
    private DumpWriter dumpWriter;

    @Autowired
    private PageReplacementService pageReplacementService;

    @Resource
    private Map<String, Long> numPagesEstimated;

    @Value("${replacer.dump.batch.chunk.size}")
    private int chunkSize;

    // Current page values
    private final StringBuilder currentChars = new StringBuilder(5000);
    private String currentTitle;
    private int currentNamespace;
    private int currentId;
    private String currentTimestamp;
    private String currentContent;

    // Indexation status
    private boolean running = false;
    private long numPagesRead = 0L;
    private long numPagesProcessed = 0L;
    private Long start = null;
    private Long end = null;

    @Setter
    private WikipediaLanguage lang;

    @Setter
    private Path latestDumpFile;

    private List<List<ReplacementEntity>> toWrite;

    @PostConstruct
    public void initializeToWrite() {
        this.toWrite = new ArrayList<>(chunkSize);
    }

    @Override
    public void startDocument() {
        LOGGER.trace("START Dump Job Execution");

        // Reset indexing status
        this.running = true;
        this.numPagesRead = 0L;
        this.numPagesProcessed = 0L;
        this.start = Instant.now().toEpochMilli();
        this.end = null;
    }

    @Override
    public void endDocument() {
        // Finish indexing status
        this.running = false;
        this.end = Instant.now().toEpochMilli();

        dumpWriter.write(toWrite);
        pageReplacementService.finish(lang);

        LOGGER.warn("END Dump Job Execution: {}", this.getDumpIndexingStatus());
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
        IndexablePage dumpPage = IndexablePage
            .builder()
            .id(this.currentId)
            .lang(this.lang)
            .title(this.currentTitle)
            .namespace(WikipediaNamespace.valueOf(this.currentNamespace))
            .lastUpdate(DateUtils.parseWikipediaTimestamp(this.currentTimestamp))
            .content(this.currentContent)
            .build();

        // If return null the page is processable but nothing to do
        // If throws ReplacerException then the page is not processable
        try {
            List<ReplacementEntity> replacementEntities = dumpPageProcessor.process(dumpPage);

            this.incrementNumPagesRead();
            if (replacementEntities != null) {
                this.incrementNumPagesProcessed();
                addToWrite(replacementEntities);
            }
        } catch (ReplacerException e) {
            // Page not processable
        }
    }

    private void incrementNumPagesRead() {
        this.numPagesRead++;
    }

    private void incrementNumPagesProcessed() {
        this.numPagesProcessed++;
    }

    private void addToWrite(List<ReplacementEntity> replacementEntities) {
        this.toWrite.add(replacementEntities);
        if (this.toWrite.size() >= chunkSize) {
            dumpWriter.write(this.toWrite);
            this.toWrite.clear();
        }
    }

    DumpIndexingStatus getDumpIndexingStatus() {
        if (this.start == null) {
            return DumpIndexingStatus.ofEmpty();
        } else {
            return DumpIndexingStatus
                .builder()
                .running(this.running)
                .numPagesRead(this.numPagesRead)
                .numPagesProcessed(this.numPagesProcessed)
                .numPagesEstimated(numPagesEstimated.get(this.lang.getCode()))
                .dumpFileName(this.latestDumpFile.getFileName().toString())
                .start(this.start)
                .end(this.end)
                .build();
        }
    }
}
