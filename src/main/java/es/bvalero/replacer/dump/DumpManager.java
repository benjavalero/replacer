package es.bvalero.replacer.dump;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.jetbrains.annotations.NotNull;
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
import java.util.Date;

/**
 * Class in charge of indexing the latest dump.
 * The most important method performs different actions for each article found in the dump,
 * like finding the misspellings and storing them in the database.
 * This indexation will be done periodically or manually from @{@link DumpController}.
 * The status of the indexation will be stored in a @{@link DumpStatus}.
 */
@Component
class DumpManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DumpManager.class);

    @Autowired
    private DumpHandler dumpHandler;

    @Autowired
    private DumpFinder dumpFinder;

    @Value("${replacer.dump.folder.path:}")
    private String dumpFolderPath;

    private DumpStatus status = new DumpStatus();

    private String getDumpFolderPath() {
        return dumpFolderPath;
    }

    void setDumpFolderPath(String dumpFolderPath) {
        this.dumpFolderPath = dumpFolderPath;
    }

    @NotNull
    DumpStatus getStatus() {
        this.status.setNumProcessedItems(dumpHandler.getNumProcessedItems());
        return status;
    }

    /**
     * Check weekly if there is a new dump to index.
     * The dump will be ignored if it is previous to the last index date.
     * If the system is redeployed, the last index date is lost, so any dump is considered as new.
     */
    @Scheduled(fixedDelay = 7 * 3600 * 24 * 1000, initialDelay = 3600 * 24 * 1000)
    @Async
    void runIndexation() {
        // TODO Externalize frequency to re-index the dump
        // Start the task
        if (getStatus().isRunning()) {
            // This should not happen but we check it just in case
            return;
        } else {
            getStatus().setRunning(true);
            getStatus().setStartDate(new Date());
        }

        try {
            File dumpFolderFile = new File(getDumpFolderPath());

            // Find the latest dump and check if we should parse again
            // In case of re-deployment of the application we will always parse
            DumpFile latestDumpFile = dumpFinder.findLatestDumpFile(dumpFolderFile);
            if (getStatus().getEndDate() != null && !getStatus().getEndDate().before(latestDumpFile.getDate())) {
                return;
            }

            parseDumpFile(latestDumpFile.getFile());

            LOGGER.info("Total number of articles processed: {}", getStatus().getNumProcessedItems());
        } catch (FileNotFoundException e) {
            LOGGER.error("Latest dump file not found", e);
        } catch (@NotNull ParserConfigurationException | SAXException | IOException e) {
            LOGGER.error("Error parsing dump file", e);
        } finally {
            getStatus().setEndDate(new Date());
            getStatus().setRunning(false);
        }
    }

    private void parseDumpFile(@NotNull File dumpFile)
            throws ParserConfigurationException, SAXException, IOException {
        LOGGER.info("Start parsing dump file: {}...", dumpFile);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        SAXParser saxParser = factory.newSAXParser();
        InputStream xmlInput = new BZip2CompressorInputStream(new FileInputStream(dumpFile));

        saxParser.parse(xmlInput, dumpHandler);
        xmlInput.close();

        LOGGER.info("Finished parsing dump file: {}", dumpFile);
    }

}
