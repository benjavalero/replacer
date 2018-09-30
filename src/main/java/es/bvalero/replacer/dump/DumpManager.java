package es.bvalero.replacer.dump;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.intellij.lang.annotations.RegExp;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

/**
 * Find the Wikipedia dumps in the filesystem where the application runs.
 * This indexation will be done weekly, or manually from @{@link DumpController}.
 * The dumps are parsed with @{@link DumpHandler}.
 * Each article found in the dump is processed in @{@link DumpArticleProcessor}.
 */
@Component
class DumpManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DumpManager.class);

    @Value("${replacer.dump.folder.path:}")
    private String dumpFolderPath;

    @Autowired
    private DumpArticleProcessor dumpArticleProcessor;

    private DumpHandler dumpHandler = new DumpHandler(dumpArticleProcessor);

    // Statistics
    private String latestDumpFile = "-";
    private boolean running = false;
    private long numArticlesEstimation = 3666708; // Rough amount of articles to be read

    @TestOnly
    void setDumpFolderPath(String dumpFolderPath) {
        this.dumpFolderPath = dumpFolderPath;
    }

    @TestOnly
    String getLatestDumpFile() {
        return latestDumpFile;
    }

    @TestOnly
    void setLatestDumpFile(String latestDumpFile) {
        this.latestDumpFile = latestDumpFile;
    }

    @TestOnly
    void setRunning() {
        this.running = true;
    }

    @TestOnly
    void setNumArticlesEstimation(long numArticlesEstimation) {
        this.numArticlesEstimation = numArticlesEstimation;
    }

    /* FIND DUMP FILE */

    /**
     * @return The path of latest available dump file from Wikipedia.
     */
    File findLatestDumpFile() throws DumpException {
        // Find the file in the latest folder
        // It may be possible that a folder does not contain the file because it is not processed yet
        File dumpFolderFile = new File(dumpFolderPath);
        File[] dumpSubFolders = dumpFolderFile.listFiles((dir, name) -> {
            // The sub-folders names are all numbers, e. g. 20170820
            @RegExp String subFolderRegex = "\\d+";
            return name.matches(subFolderRegex);
        });

        if (dumpSubFolders == null || dumpSubFolders.length == 0) {
            throw new DumpException("Sub-folders not found in dump path: " + dumpFolderPath);
        }

        Arrays.sort(dumpSubFolders, Collections.reverseOrder());
        // Try with all the folders starting for the latest
        for (File dumpSubFolder : dumpSubFolders) {
            String dumpFileName = "eswiki-" + dumpSubFolder.getName() + "-pages-articles.xml.bz2";
            File dumpFile = new File(dumpSubFolder, dumpFileName);
            if (dumpFile.exists()) {
                return dumpFile;
            }
        }

        // If we get here no dump file has been found
        throw new DumpException("No dump file has been found in dump path: " + dumpFolderPath);
    }

    /* PARSE DUMP FILE */

    @TestOnly
    void parseDumpFile(File dumpFile) throws DumpException {
        parseDumpFile(dumpFile, false);
    }

    private void parseDumpFile(File dumpFile, boolean forceProcess) throws DumpException {
        LOGGER.info("Start parsing dump file: {}...", dumpFile);

        // Check just in case that the handler is not running
        if (running) {
            LOGGER.info("Dump indexation is already running");
            return;
        }

        try (InputStream xmlInput = new BZip2CompressorInputStream(new FileInputStream(dumpFile))) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            SAXParser saxParser = factory.newSAXParser();

            // Start statistics
            running = true;
            this.latestDumpFile = dumpFile.getPath();

            // Parse with the Dump Handler
            dumpHandler = createDumpHandler(forceProcess);
            saxParser.parse(xmlInput, dumpHandler);
        } catch (IOException e) {
            throw new DumpException("Dump file not valid: " + dumpFile, e);
        } catch (ParserConfigurationException | SAXException e) {
            throw new DumpException("SAX Error parsing dump file: " + dumpFile, e);
        }

        LOGGER.info("Finish parsing dump file: {}", dumpFile);
    }

    // Just for make easier the mock of the handler for testing
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
            File latestDumpFile = findLatestDumpFile();

            // We check against the latest dump file processed
            if (!latestDumpFile.getPath().equals(this.latestDumpFile) || forceProcessDump) {
                // Start process
                parseDumpFile(findLatestDumpFile(), forceProcessDumpArticles);

                // Once finished we mark the file as processed
                this.running = false;
                this.numArticlesEstimation = dumpHandler.getNumArticlesRead();
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
            numArticlesEstimation += 1000;
        }

        return new DumpProcessStatus(
                running,
                dumpHandler.isForceProcess(),
                dumpHandler.getNumArticlesRead(),
                dumpHandler.getNumArticlesProcessed(),
                new File(latestDumpFile).getName(),
                getAverageTimePerArticle(),
                getTime(),
                String.format("%.2f", dumpHandler.getNumArticlesRead() * 100.0 / numArticlesEstimation)
        );
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
                        : (dumpHandler.getEndTime() == 0 ? 0L : dumpHandler.getEndTime() - dumpHandler.getStartTime()), // Total time
                "d:HH:mm:ss", true);
    }

}
