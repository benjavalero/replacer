package es.bvalero.replacer.dump;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.nio.file.Path;
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
    private ReplacementCache replacementCache;

    @Resource
    private Map<String, Integer> numPagesEstimated;

    @Value("${replacer.dump.batch.chunk.size}")
    private int chunkSize;

    // Current article values
    private final StringBuilder currentChars = new StringBuilder(5000);
    private String currentTitle;
    private int currentNamespace;
    private int currentId;
    private String currentTimestamp;
    private String currentContent;

    private DumpIndexingStatus dumpIndexingStatus = DumpIndexingStatus.ofEmpty();

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
        this.dumpIndexingStatus =
            DumpIndexingStatus.of(
                this.latestDumpFile.getFileName().toString(),
                numPagesEstimated.get(this.lang.getCode())
            );
    }

    @Override
    public void endDocument() {
        this.dumpIndexingStatus.finish();
        dumpWriter.write(toWrite);
        replacementCache.finish(lang);

        LOGGER.warn("END Dump Job Execution: {}", this.dumpIndexingStatus);
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
            .id(this.currentId)
            .lang(this.lang)
            .title(this.currentTitle)
            .namespace(WikipediaNamespace.valueOf(this.currentNamespace))
            .lastUpdate(WikipediaPage.parseWikipediaTimestamp(this.currentTimestamp))
            .content(this.currentContent)
            .build();

        // If return null the it is processable but nothing to do
        // If throws ReplacerException then the page is not processable
        try {
            List<ReplacementEntity> replacementEntities = dumpPageProcessor.process(dumpPage);

            this.dumpIndexingStatus.incrementNumPagesRead();
            if (replacementEntities != null) {
                this.dumpIndexingStatus.incrementNumPagesProcessed();
                addToWrite(replacementEntities);
            }
        } catch (ReplacerException e) {
            // Page not processable
        }
    }

    private void addToWrite(List<ReplacementEntity> replacementEntities) {
        this.toWrite.add(replacementEntities);
        if (this.toWrite.size() >= chunkSize) {
            dumpWriter.write(this.toWrite);
            this.toWrite.clear();
        }
    }

    DumpIndexingStatus getDumpIndexingStatus() {
        return this.dumpIndexingStatus;
    }
}
