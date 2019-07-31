package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.ArticleService;
import es.bvalero.replacer.article.Replacement;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import lombok.extern.slf4j.Slf4j;
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
        LOGGER.debug("START Handle dump document: {} - Force: {}", latestDumpFile, forceProcess);

        running = true;
        numArticlesRead = 0L;
        numArticlesProcessed = 0L;
        startTime = Instant.now();
    }

    @Override
    public void endDocument() {
        LOGGER.info("END handle dump document: {}", latestDumpFile);

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
                .id(currentId)
                .title(currentTitle)
                .namespace(WikipediaNamespace.valueOf(currentNamespace))
                .lastUpdate(WikipediaPage.parseWikipediaTimestamp(currentTimestamp))
                .content(currentContent)
                .queryTimestamp(WikipediaPage.formatWikipediaTimestamp(LocalDateTime.now()))
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
                .running(running)
                .forceProcess(forceProcess)
                .numArticlesRead(numArticlesRead)
                .numArticlesProcessed(numArticlesProcessed)
                .dumpFileName(latestDumpFile == null ? "" : latestDumpFile.getFileName().toString())
                .start(startTime == null ? null : startTime.toEpochMilli())
                .end(endTime == null ? null : endTime.toEpochMilli())
                .build();
    }

    private class DumpArticleCache {
        private static final int CACHE_SIZE = 1000;
        private int maxCachedId;
        private Map<Integer, Collection<Replacement>> replacementMap = new HashMap<>(CACHE_SIZE);

        Collection<Replacement> findDatabaseReplacements(int articleId) {
            // Load the cache the first time or when needed
            if (maxCachedId == 0 || articleId > maxCachedId) {
                cleanCache();

                int minId = maxCachedId + 1;
                maxCachedId += CACHE_SIZE;
                loadCache(minId, maxCachedId);
            }

            Collection<Replacement> replacements = replacementMap.getOrDefault(articleId, Collections.emptySet());
            replacementMap.remove(articleId); // No need to check if the ID exists

            return replacements;
        }

        private void loadCache(int minId, int maxId) {
            LOGGER.debug("START Load replacements from database to cache. Article ID between {} and {}", minId, maxId);
            for (Replacement replacement : articleService.findDatabaseReplacementByArticles(minId, maxId)) {
                if (!replacementMap.containsKey(replacement.getArticleId())) {
                    replacementMap.put(replacement.getArticleId(), new HashSet<>());
                }
                replacementMap.get(replacement.getArticleId()).add(replacement);
            }
            LOGGER.debug("END Load replacements from database to cache. Articles cached: {}", replacementMap.size());
        }

        private void cleanCache() {
            // Clear the cache if obsolete (we assume the dump articles are in order)
            // The remaining cached articles are not in the dump so we remove them from DB
            LOGGER.debug("START Delete obsolete articles in DB: {}", replacementMap.keySet());
            articleService.deleteArticles(replacementMap.keySet());
            replacementMap = new HashMap<>(CACHE_SIZE);
            LOGGER.debug("END Delete obsolete articles in DB");
        }
    }

}
