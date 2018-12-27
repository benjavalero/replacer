package es.bvalero.replacer.dump;

import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Handler to parse a Wikipedia XML dump.
 */
@Component
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
    private static final long NUM_ARTICLES = 3718238L; // Rough amount of articles to be read
    private static final String DURATION_FORMAT = "d:HH:mm:ss";
    @RegExp
    private static final String PERCENTAGE_FORMAT = "%.2f";

    @Autowired
    private DumpArticleProcessor dumpArticleProcessor;

    // Current article values
    private StringBuilder currentChars = new StringBuilder(5000);
    private String currentTitle;
    private WikipediaNamespace currentNamespace;
    private int currentId;
    private LocalDateTime currentTimestamp;
    private String currentContent;

    // Status
    private boolean running = false;
    private Path latestDumpFile = null;
    private boolean forceProcess;
    private long numArticlesRead;
    private long numArticlesProcessed;
    private Instant startTime;
    private Instant endTime;

    boolean isRunning() {
        return running;
    }

    void setLatestDumpFile(Path latestDumpFile) {
        this.latestDumpFile = latestDumpFile;
    }

    void setForceProcess(boolean forceProcess) {
        this.forceProcess = forceProcess;
    }

    @Override
    public void startDocument() {
        LOGGER.info("Start parsing dump document...");

        running = true;
        numArticlesRead = 0L;
        numArticlesProcessed = 0L;
        startTime = Instant.now();
    }

    @Override
    public void endDocument() {
        LOGGER.info("Finished parsing dump document...");

        running = false;
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

    LocalDateTime parseWikipediaDate(CharSequence dateStr) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(WIKIPEDIA_DATE_PATTERN);
        return LocalDateTime.from(dateFormat.parse(dateStr));
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

    DumpProcessStatus getProcessStatus() {
        return DumpProcessStatus.builder()
                .setRunning(running)
                .setForceProcess(forceProcess)
                .setNumArticlesRead(numArticlesRead)
                .setNumArticlesProcessed(numArticlesProcessed)
                .setDumpFileName(latestDumpFile == null ? "-" : latestDumpFile.getFileName().toString())
                .setAverage(getAverageTimePerArticle().toMillis())
                .setTime(getTime())
                .setProgress(getProgressPercentage())
                .build();
    }

    private Duration getAverageTimePerArticle() {
        Duration average;
        if (numArticlesRead == 0L) {
            average = Duration.ofMillis(0L);
        } else if (running) {
            Duration elapsed = Duration.between(startTime, Instant.now());
            average = Duration.ofMillis(elapsed.toMillis() / numArticlesRead);
        } else if (endTime == null) {
            average = Duration.ofMillis(0L);
        } else {
            Duration elapsed = Duration.between(startTime, endTime);
            average = Duration.ofMillis(elapsed.toMillis() / numArticlesRead);
        }
        return average;
    }

    private String getTime() {
        Duration time;
        if (running) {
            long numArticlesToRead = Math.max(NUM_ARTICLES, numArticlesRead) - numArticlesRead;
            time = Duration.ofMillis(numArticlesToRead * getAverageTimePerArticle().toMillis());
        } else if (endTime == null) {
            time = Duration.ofMillis(0L);
        } else {
            time = Duration.between(startTime, endTime);
        }
        return DurationFormatUtils.formatDuration(time.toMillis(), DURATION_FORMAT, true);
    }

    private String getProgressPercentage() {
        String progress = null;
        // This only has sense if the dump process is running
        if (running) {
            // If we read more articles than expected, the result will always be 100%
            double percent = (double) numArticlesRead * 100.0 / (double) Math.max(NUM_ARTICLES, numArticlesRead);
            progress = String.format(PERCENTAGE_FORMAT, percent);
        }
        return progress;
    }

}
