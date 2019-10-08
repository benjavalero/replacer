package es.bvalero.replacer.dump;

import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.nio.file.Path;
import java.util.Optional;

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
    private ModelMapper modelMapper;

    @Autowired
    private IndexationRepository indexationRepository;

    @Autowired
    private DumpArticleProcessor dumpArticleProcessor;

    // Current article values
    private StringBuilder currentChars = new StringBuilder(5000);
    private String currentTitle;
    private int currentNamespace;
    private int currentId;
    private String currentTimestamp;
    private String currentContent;

    // Status
    private DumpIndexation status = null;

    @Setter
    private Path latestDumpFile = null;
    @Setter
    private boolean forceProcess;

    @Override
    public void startDocument() {
        LOGGER.debug("START Handle dump document: {} - Force: {}", latestDumpFile, forceProcess);

        this.status = new DumpIndexation(latestDumpFile.getFileName().toString(), forceProcess);
    }

    @Override
    public void endDocument() {
        this.status.finish();
        dumpArticleProcessor.finishOverallProcess();
        indexationRepository.save(convertToEntity(this.status));
        LOGGER.info("END handle dump document: {}", getProcessStatus());
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
        this.status.incrementNumArticlesRead();
        DumpArticle dumpArticle = DumpArticle.builder()
                .id(currentId)
                .title(currentTitle)
                .namespace(WikipediaNamespace.valueOf(currentNamespace))
                .lastUpdate(WikipediaPage.parseWikipediaTimestamp(currentTimestamp))
                .content(currentContent)
                .build();

        try {
            if (dumpArticle.isProcessable()) {
                this.status.incrementNumArticlesProcessable();
                boolean articleProcessed = processArticle(dumpArticle);
                if (articleProcessed) {
                    this.status.incrementNumArticlesProcessed();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error processing dump page: {}", currentTitle, e);
        }
    }

    private boolean processArticle(DumpArticle dumpArticle) {
        return dumpArticleProcessor.processArticle(dumpArticle);
    }

    DumpIndexation getProcessStatus() {
        if (this.status == null) {
            this.status = findLastIndexation().orElse(new DumpIndexation());
        }
        return this.status;
    }

    private Optional<DumpIndexation> findLastIndexation() {
        return indexationRepository.findByOrderByIdDesc(PageRequest.of(0, 1)).stream().findAny()
                .map(this::convertToDto);
    }

    private DumpIndexation convertToDto(IndexationEntity entity) {
        return modelMapper.map(entity, DumpIndexation.class);
    }

    private IndexationEntity convertToEntity(DumpIndexation dto) {
        return modelMapper.map(dto, IndexationEntity.class);
    }

}
