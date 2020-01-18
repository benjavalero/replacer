package es.bvalero.replacer.dump;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import es.bvalero.replacer.ReplacerException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Find the Wikipedia dumps in the filesystem where the application runs.
 * This indexation will be done weekly, or manually from @{@link DumpController}.
 * The dumps are parsed with @{@link DumpHandler}.
 * Each article found in the dump is processed in @{@link DumpArticleProcessor}.
 */
@Slf4j
@Component
class DumpManager {

    @Setter
    @Value("${replacer.dump.index.wait:}")
    private int dumpIndexWait;

    @Autowired
    private DumpFinder dumpFinder;

    @Autowired
    private DumpHandler dumpHandler;

    /**
     * Check if there is a new dump to process.
     */
    @Scheduled(fixedDelayString = "${replacer.dump.index.delay}")
    void processDumpScheduled() {
        LOGGER.info("EXECUTE Scheduled weekly index of the last dump");
        processLatestDumpFile(false);
    }

    /**
     * Find the latest dump file and process it.
     *
     * @param forceProcessDump Force processing again an already indexed dump.
     */
    // In order to be asynchronous it must be public and called externally:
    // https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/annotation/EnableAsync.html
    @SuppressWarnings("WeakerAccess")
    @Async
    public void processLatestDumpFile(boolean forceProcessDump) {
        LOGGER.info("START Indexation of latest dump file. Force: {}", forceProcessDump);

        // Check just in case the handler is already running
        if (dumpHandler.getProcessStatus().isRunning()) {
            LOGGER.info("END Indexation of latest dump file. Dump indexation is already running.");
            return;
        }

        try {
            Path latestDumpFileFound = dumpFinder.findLatestDumpFile();
            String latestDumpFileName = latestDumpFileFound.getFileName().toString();

            // We check against the latest dump file processed
            if ((!latestDumpFileName.equals(findLatestDumpFileNameFromDatabase())
                    && isDumpFileOldEnough(latestDumpFileFound)) || forceProcessDump) {
                parseDumpFile(latestDumpFileFound, forceProcessDump);
                LOGGER.info("END Indexation of latest dump file: {}", latestDumpFileFound);
            } else {
                LOGGER.info("END Indexation of latest dump file. Latest dump file already indexed or not old enough.");
            }
        } catch (DumpException | ReplacerException e) {
            LOGGER.error("Error indexing latest dump file", e);
        }
    }

    // Check if the dump file is old enough, i. e. modified more than a day ago
    private boolean isDumpFileOldEnough(Path dumpFile) {
        try {
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(dumpIndexWait);
            FileTime fileTime = Files.getLastModifiedTime(dumpFile);
            return fileTime.toInstant().isBefore(oneDayAgo.toInstant(ZoneOffset.UTC));
        } catch (IOException e) {
            return false;
        }
    }

    private String findLatestDumpFileNameFromDatabase() {
        // At this point we know that the indexation is not running
        // so the status of the handler matches with last finished indexation in database
        return dumpHandler.getProcessStatus().getDumpFileName();
    }

    void parseDumpFile(Path dumpFile, boolean forceProcess) throws DumpException {
        LOGGER.info("START Parse dump file: {}", dumpFile);

        try (InputStream xmlInput = new BZip2CompressorInputStream(Files.newInputStream(dumpFile), true)) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            SAXParser saxParser = factory.newSAXParser();

            dumpHandler.setLatestDumpFile(dumpFile);
            dumpHandler.setForceProcess(forceProcess);
            saxParser.parse(xmlInput, dumpHandler);
            LOGGER.info("END Parse dump file: {}", dumpFile);
        } catch (IOException e) {
            throw new DumpException("Dump file not valid", e);
        } catch (ParserConfigurationException | SAXException e) {
            throw new DumpException("SAX Error parsing dump file", e);
        }
    }

    DumpIndexation getDumpStatus() {
        return dumpHandler.getProcessStatus();
    }

}
