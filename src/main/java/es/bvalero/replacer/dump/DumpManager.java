package es.bvalero.replacer.dump;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Find the Wikipedia dumps in the filesystem where the application runs.
 * This indexation will be done weekly, or manually from @{@link DumpController}.
 * The dumps are parsed with @{@link DumpHandler}.
 * Each article found in the dump is processed in @{@link DumpArticleProcessor}.
 */
@Component
class DumpManager {

    static final String DUMP_NAME_FORMAT = "eswiki-%s-pages-articles.xml.bz2";
    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(DumpManager.class);
    // Rough amount of articles to be read
    private static final long NUM_ARTICLES = 3666708L;
    @RegExp
    private static final String REGEX_DUMP_FOLDER = "\\d+";
    private static final Pattern PATTERN_DUMP_FOLDER = Pattern.compile(REGEX_DUMP_FOLDER);
    private static final String PERCENTAGE_FORMAT = "%.2f";
    private static final String DURATION_FORMAT = "d:HH:mm:ss";

    @Value("${replacer.dump.folder.path:}")
    private String dumpFolderPath;

    @Autowired
    private DumpArticleProcessor dumpArticleProcessor;

    private DumpHandler dumpHandler = new DumpHandler(dumpArticleProcessor);

    // Statistics
    private Path latestDumpFile;
    private boolean running;
    private long numArticlesEstimation = NUM_ARTICLES;

    @TestOnly
    void setDumpFolderPath(String dumpFolderPath) {
        this.dumpFolderPath = dumpFolderPath;
    }

    @TestOnly
    Path getLatestDumpFile() {
        return latestDumpFile;
    }

    @TestOnly
    void setLatestDumpFile(Path latestDumpFile) {
        this.latestDumpFile = latestDumpFile;
    }

    @TestOnly
    void setRunning() {
        running = true;
    }

    @TestOnly
    void setNumArticlesEstimation(long numArticlesEstimation) {
        this.numArticlesEstimation = numArticlesEstimation;
    }

    /* FIND DUMP FILE */

    /**
     * @return The path of latest available dump file from Wikipedia.
     */
    Path findLatestDumpFile() throws DumpException {
        // Find the file in the latest folder
        // It may be possible that a folder does not contain the file because it is not processed yet
        Path dumpFolderFile = Paths.get(dumpFolderPath);
        try {
            List<Path> dumpSubFolders = Files.list(dumpFolderFile)
                    .filter(folder -> PATTERN_DUMP_FOLDER.matcher(folder.getFileName().toString()).matches())
                    .sorted(Collections.reverseOrder())
                    .collect(Collectors.toList());

            if (dumpSubFolders.isEmpty()) {
                throw new DumpException("Sub-folders not found in dump path");
            }

            // Try with all the folders starting for the latest
            for (Path dumpSubFolder : dumpSubFolders) {
                String dumpFileName = String.format(DUMP_NAME_FORMAT, dumpSubFolder.getFileName());
                Path dumpFile = dumpSubFolder.resolve(dumpFileName);
                if (Files.exists(dumpFile)) {
                    return dumpFile;
                }
            }

            // If we get here no dump file has been found
            throw new DumpException("No dump file has been found in dump path");
        } catch (IOException e) {
            throw new DumpException("Error listing files in dump path", e);
        }
    }

    /* PARSE DUMP FILE */

    @TestOnly
    void parseDumpFile(Path dumpFile) throws DumpException {
        parseDumpFile(dumpFile, false);
    }

    private void parseDumpFile(Path dumpFile, boolean forceProcess) throws DumpException {
        LOGGER.info("Start parsing dump file: {}...", dumpFile);

        // Check just in case that the handler is not running
        if (running) {
            LOGGER.info("Dump indexation is already running");
            return;
        }

        try (InputStream xmlInput = new BZip2CompressorInputStream(Files.newInputStream(dumpFile))) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            SAXParser saxParser = factory.newSAXParser();

            // Start statistics
            running = true;
            latestDumpFile = dumpFile;

            // Parse with the Dump Handler
            dumpHandler = createDumpHandler(forceProcess);
            saxParser.parse(xmlInput, dumpHandler);
        } catch (IOException e) {
            throw new DumpException("Dump file not valid", e);
        } catch (ParserConfigurationException | SAXException e) {
            throw new DumpException("SAX Error parsing dump file", e);
        }

        LOGGER.info("Finish parsing dump file: {}", dumpFile);
    }

    // Just to make easier the mock of the handler for testing
    DumpHandler createDumpHandler(boolean forceProcess) {
        return new DumpHandler(dumpArticleProcessor, forceProcess);
    }

    /* PROCESS DUMP FILE */

    /**
     * Check weekly if there is a new dump to process (with a one-day delay)
     */
    @Scheduled(fixedDelay = 7 * 3600 * 24 * 1000, initialDelay = 3600 * 24 * 1000)
    @Async
    void processLatestDumpFile() {
        processLatestDumpFile(true, false);
    }

    /**
     * Find the latest dump file and process it.
     *
     * @param forceProcessDump         When triggered manually we always process the latest dump although it has been
     *                                 already processed.
     * @param forceProcessDumpArticles Force processing all dump articles event if they have not been modified since
     *                                 last processing.
     */
    @Async
    void processLatestDumpFile(boolean forceProcessDump, boolean forceProcessDumpArticles) {
        LOGGER.info("Start process latest dump file. Force dump process: {}. Force article process: {}",
                forceProcessDump, forceProcessDumpArticles);
        try {
            // Find the latest dump file
            Path latestDumpFileFound = findLatestDumpFile();

            // We check against the latest dump file processed
            if (!latestDumpFileFound.equals(latestDumpFile) || forceProcessDump) {
                // Start process
                parseDumpFile(latestDumpFileFound, forceProcessDumpArticles);

                // Once finished we mark the file as processed
                running = false;
                numArticlesEstimation = dumpHandler.getNumArticlesRead();
            } else {
                LOGGER.info("Latest dump file found already indexed");
            }
        } catch (DumpException e) {
            LOGGER.error("Error processing last dump file", e);
        }
    }

    DumpProcessStatus getProcessStatus() {
        // In case the dump contains more articles than estimated
        if (dumpHandler.getNumArticlesRead() > numArticlesEstimation) {
            numArticlesEstimation++;
        }

        return DumpProcessStatus.builder()
                .setRunning(running)
                .setForceProcess(dumpHandler.isForceProcess())
                .setNumArticlesRead(dumpHandler.getNumArticlesRead())
                .setNumArticlesProcessed(dumpHandler.getNumArticlesProcessed())
                .setDumpFileName(latestDumpFile == null ? "-" : latestDumpFile.getFileName().toString())
                .setAverage(getAverageTimePerArticle())
                .setTime(getTime())
                .setProgress(String.format(PERCENTAGE_FORMAT,
                        (double) dumpHandler.getNumArticlesRead() * 100.0 / (double) numArticlesEstimation))
                .build();
    }

    private long getAverageTimePerArticle() {
        // The process may not be running
        if (dumpHandler.getNumArticlesRead() == 0L) {
            return 0L;
        } else if (running) {
            return (System.currentTimeMillis() - dumpHandler.getStartTime()) / dumpHandler.getNumArticlesRead();
        } else {
            return dumpHandler.getEndTime() == 0L ? 0L
                    : (dumpHandler.getEndTime() - dumpHandler.getStartTime()) / dumpHandler.getNumArticlesRead();
        }
    }

    private String getTime() {
        // The final process time if it is not running, and the current process time if it is still running.
        return DurationFormatUtils.formatDuration(running
                        ? (numArticlesEstimation - dumpHandler.getNumArticlesRead()) * getAverageTimePerArticle() // ETA
                        : (dumpHandler.getEndTime() == 0L ? 0L : dumpHandler.getEndTime() - dumpHandler.getStartTime()), // Total time
                DURATION_FORMAT, true);
    }

}
