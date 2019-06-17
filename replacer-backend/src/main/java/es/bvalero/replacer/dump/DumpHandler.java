package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.ArticleService;
import es.bvalero.replacer.article.Replacement;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Handler to parse a Wikipedia XML dump.
 */
@Component
class DumpHandler extends DefaultHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DumpHandler.class);
    private static final String TITLE_TAG = "title";
    private static final String NAMESPACE_TAG = "ns";
    private static final String ID_TAG = "id";
    private static final String TIMESTAMP_TAG = "timestamp";
    private static final String TEXT_TAG = "text";
    private static final String PAGE_TAG = "page";

    @Autowired
    private DumpArticleProcessor dumpArticleProcessor;

    @Autowired
    private ArticleService articleService;

    // Current article values
    private StringBuilder currentChars = new StringBuilder(5000);
    private String currentTitle;
    private int currentNamespace;
    private int currentId;
    private String currentTimestamp;
    private String currentContent;

    // Status
    private boolean running = false;
    private Path latestDumpFile = null;
    private boolean forceProcess;
    private long numArticlesRead;
    private long numArticlesProcessed;
    private Instant startTime;
    private Instant endTime;

    // Get database replacements in batches to improve performance
    private DumpArticleCache cache = new DumpArticleCache();

    boolean isRunning() {
        return running;
    }

    Path getLatestDumpFile() {
        return latestDumpFile;
    }

    void setLatestDumpFile(Path latestDumpFile) {
        this.latestDumpFile = latestDumpFile;
    }

    void setForceProcess(boolean forceProcess) {
        this.forceProcess = forceProcess;
    }

    @Override
    public void startDocument() {
        LOGGER.info("Start dump document");

        running = true;
        numArticlesRead = 0L;
        numArticlesProcessed = 0L;
        startTime = Instant.now();
    }

    @Override
    public void endDocument() {
        LOGGER.info("End dump document");

        running = false;
        endTime = Instant.now();
        articleService.flushReplacementsInBatch();
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
                .setId(currentId)
                .setTitle(currentTitle)
                .setNamespace(currentNamespace)
                .setTimestamp(currentTimestamp)
                .setContent(currentContent)
                .setQueryTimestamp(WikipediaPage.formatWikipediaTimestamp(LocalDateTime.now()))
                .build();

        try {
            boolean articleProcessed = processArticle(dumpArticle);
            if (articleProcessed) {
                numArticlesProcessed++;
            }
        } catch (Exception e) {
            LOGGER.error("Error processing dump page: {}", currentTitle, e);
        }
    }

    private boolean processArticle(WikipediaPage dumpArticle) {
        return dumpArticleProcessor.processArticle(dumpArticle, cache.findDatabaseReplacements(dumpArticle.getId()), forceProcess);
    }

    DumpProcessStatus getProcessStatus() {
        return DumpProcessStatus.builder()
                .setRunning(running)
                .setForceProcess(forceProcess)
                .setNumArticlesRead(numArticlesRead)
                .setNumArticlesProcessed(numArticlesProcessed)
                .setDumpFileName(latestDumpFile == null ? "" : latestDumpFile.getFileName().toString())
                .setStart(startTime == null ? null : startTime.toEpochMilli())
                .setEnd(endTime == null ? null : endTime.toEpochMilli())
                .build();
    }

    private class DumpArticleCache {
        private static final int CACHE_SIZE = 1000;
        private int maxCachedId;
        private Map<Integer, Collection<Replacement>> replacementMap = new HashMap<>(CACHE_SIZE);

        Collection<Replacement> findDatabaseReplacements(int articleId) {
            // Load the cache the first time
            if (maxCachedId == 0) {
                loadCache(1);
            }

            Collection<Replacement> replacements = replacementMap.getOrDefault(articleId, Collections.emptySet());
            replacementMap.remove(articleId); // No need to check if the ID exists

            if (articleId >= maxCachedId) {
                cleanCache();
                loadCache(Math.max(maxCachedId + 1, articleId));
            }

            return replacements;
        }

        private void loadCache(int id) {
            LOGGER.debug("Load replacements from database to cache. Min ID: {}", id);
            maxCachedId = id + CACHE_SIZE - 1;
            for (Replacement replacement : articleService.findDatabaseReplacementByArticles(id, maxCachedId)) {
                if (!replacementMap.containsKey(replacement.getArticleId())) {
                    replacementMap.put(replacement.getArticleId(), new HashSet<>());
                }
                replacementMap.get(replacement.getArticleId()).add(replacement);
            }
        }

        private void cleanCache() {
            // Clear the cache if obsolete (we assume the dump articles are in order)
            // The remaining cached articles are not in the dump so we remove them from DB
            LOGGER.debug("Delete obsolete articles in DB: {}", replacementMap.size());
            articleService.deleteArticles(replacementMap.keySet());
            replacementMap = new HashMap<>(CACHE_SIZE);
        }
    }

}
