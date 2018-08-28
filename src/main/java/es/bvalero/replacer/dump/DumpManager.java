package es.bvalero.replacer.dump;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
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
import java.io.*;

/**
 * Class in charge of indexing the latest dump.
 * The most important method performs different actions for each article found in the dump,
 * like finding the misspellings and storing them in the database.
 * This indexation will be done weekly or manually from @{@link DumpController}.
 * The status of the indexation will be stored in a @{@link DumpStatus}.
 */
@Component
class DumpManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DumpManager.class);
    @Autowired
    private DumpFinder dumpFinder;
    @Value("${replacer.dump.folder.path:}")
    private String dumpFolderPath;
    @Autowired
    private DumpProcessor dumpProcessor;

    private DumpHandler dumpHandler = new DumpHandler() {
        @Override
        void processArticle(DumpArticle article) {
            dumpProcessor.processArticle(getCurrentArticle(), this.getDumpStatus().isProcessOldArticles());
        }
    };

    // For tests
    void setDumpFolderPath(String dumpFolderPath) {
        this.dumpFolderPath = dumpFolderPath;
    }

    DumpStatus getStatus() {
        return dumpHandler.getDumpStatus();
    }

    // For tests
    DumpHandler getDumpHandler() {
        return this.dumpHandler;
    }

    /**
     * Check weekly if there is a new dump to index (with a one-day delay).
     * The dump will be ignored if it is previous to the last index date.
     * If the system is redeployed, the last index date is lost, so any dump is considered as new.
     */
    @Scheduled(fixedDelay = 7 * 3600 * 24 * 1000, initialDelay = 3600 * 24 * 1000)
    @Async
    void runIndexation() {
        runIndexation(true, false);
    }

    /**
     * Index the latest dump of Wikipedia articles.
     *
     * @param scheduledExecution True if the indexing is triggered by the scheduled task, false if triggered manually.
     * @param processOldArticles If true we reprocess all articles even if they have not been modified since last indexing.
     */
    void runIndexation(boolean scheduledExecution, boolean processOldArticles) {
        LOGGER.info("Start indexation. Scheduled execution: {}. Force processing of old articles: {}",
                scheduledExecution, processOldArticles);

        // Start the task
        if (getStatus().isRunning()) {
            LOGGER.info("Indexation is already running. Do nothing.");
            return;
        }

        try {
            File dumpFolderFile = new File(dumpFolderPath);

            // Find the latest dump and check if we should parse again
            // If the execution is manual we parse again
            // In case of re-deployment of the application we will always parse
            DumpFile latestDumpFile = dumpFinder.findLatestDumpFile(dumpFolderFile);
            LOGGER.info("Latest Dump File: {}", latestDumpFile.getFile());
            // The date of the dump file is a pure date with no time
            if (scheduledExecution && getStatus().getLastRun() != null
                    && getStatus().getLastRun() >= (latestDumpFile.getDate().getTime())) {
                LOGGER.info("Latest Dump File already indexed. Do nothing.");
                return;
            }

            parseDumpFile(latestDumpFile.getFile(), processOldArticles);
        } catch (FileNotFoundException e) {
            LOGGER.error("Latest dump file not found", e);
        }
    }

    private void parseDumpFile(File dumpFile, final boolean processOldArticles) {
        LOGGER.info("Start parsing dump file: {}...", dumpFile);

        try (InputStream xmlInput = new BZip2CompressorInputStream(new FileInputStream(dumpFile))) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            SAXParser saxParser = factory.newSAXParser();

            dumpHandler.getDumpStatus().setProcessOldArticles(processOldArticles);
            saxParser.parse(xmlInput, dumpHandler);
        } catch (IOException e) {
            LOGGER.error("Latest dump file not found or valid", e);
        } catch (ParserConfigurationException | SAXException e) {
            LOGGER.error("Error parsing dump file", e);
        } finally {
            dumpHandler.getDumpStatus().finish();
        }

        LOGGER.info("Finished parsing dump file: {}\n{}", dumpFile, dumpHandler.getDumpStatus());
    }

}
