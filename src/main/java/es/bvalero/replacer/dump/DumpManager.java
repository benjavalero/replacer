package es.bvalero.replacer.dump;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
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
import java.util.stream.Stream;

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
    private static final String REGEX_DUMP_FOLDER = "\\d+";
    private static final Pattern PATTERN_DUMP_FOLDER = Pattern.compile(REGEX_DUMP_FOLDER);

    @Value("${replacer.dump.folder.path:}")
    private String dumpFolderPath;

    @Autowired
    private DumpHandler dumpHandler;

    @TestOnly
    void setDumpFolderPath(String dumpFolderPath) {
        this.dumpFolderPath = dumpFolderPath;
    }

    /**
     * Check weekly if there is a new dump to process (with a one-day delay)
     */
    @Scheduled(fixedDelay = 7 * 3600 * 24 * 1000, initialDelay = 3600 * 24 * 1000)
    void processDumpScheduled() {
        processLatestDumpFile(false, false);
    }

    /**
     * Find the latest dump file and process it.
     *
     * @param forceProcessDump         When triggered manually we always process the latest dump although it has been
     *                                 already processed.
     * @param forceProcessDumpArticles Force processing all dump articles event if they have not been modified since
     *                                 last processing.
     */
    void processLatestDumpFile(boolean forceProcessDump, boolean forceProcessDumpArticles) {
        LOGGER.info("Start processing latest dump file. Force dump process: {}. Force article process: {}",
                forceProcessDump, forceProcessDumpArticles);

        // Check just in case the handler is already running
        if (dumpHandler.isRunning()) {
            LOGGER.info("Dump indexation is already running");
            return;
        }

        try {
            Path latestDumpFileFound = findLatestDumpFile();
            LOGGER.info("Latest dump file found: {}", latestDumpFileFound);

            // We check against the latest dump file processed
            if (!latestDumpFileFound.equals(dumpHandler.getLatestDumpFile()) || forceProcessDump) {
                parseDumpFile(latestDumpFileFound, forceProcessDumpArticles);
                LOGGER.info("Finished processing latest dump file: {}", latestDumpFileFound);
            } else {
                LOGGER.info("Latest dump file found already indexed");
            }
        } catch (DumpException e) {
            LOGGER.error("Error processing last dump file", e);
        }
    }

    /**
     * @return The path of latest available dump file from Wikipedia.
     */
    Path findLatestDumpFile() throws DumpException {
        List<Path> dumpSubFolders = findDumpFolders();

        // Try with all the folders starting for the latest
        for (Path dumpSubFolder : dumpSubFolders) {
            String dumpFileName = String.format(DUMP_NAME_FORMAT, dumpSubFolder.getFileName());
            Path dumpFile = dumpSubFolder.resolve(dumpFileName);
            if (dumpFile.toFile().exists()) {
                return dumpFile;
            }
        }

        // If we get here no dump file has been found
        throw new DumpException("No dump file has been found in dump path");
    }

    private List<Path> findDumpFolders() throws DumpException {
        try (Stream<Path> dumpSubPaths = Files.list(Paths.get(dumpFolderPath))) {
            List<Path> dumpSubFolders = dumpSubPaths
                    .filter(folder -> PATTERN_DUMP_FOLDER.matcher(folder.getFileName().toString()).matches())
                    .sorted(Collections.reverseOrder())
                    .collect(Collectors.toList());
            if (dumpSubFolders.isEmpty()) {
                throw new DumpException("Sub-folders not found in dump path");
            }
            return dumpSubFolders;
        } catch (IOException e) {
            throw new DumpException("Error listing files in dump path", e);
        }
    }

    @Async
    void parseDumpFile(Path dumpFile, boolean forceProcess) throws DumpException {
        LOGGER.info("Start parsing dump file: {}...", dumpFile);

        try (InputStream xmlInput = new BZip2CompressorInputStream(Files.newInputStream(dumpFile))) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            SAXParser saxParser = factory.newSAXParser();

            dumpHandler.setLatestDumpFile(dumpFile);
            dumpHandler.setForceProcess(forceProcess);
            saxParser.parse(xmlInput, dumpHandler);
            LOGGER.info("Finished parsing dump file: {}", dumpFile);
        } catch (IOException e) {
            throw new DumpException("Dump file not valid", e);
        } catch (ParserConfigurationException | SAXException e) {
            throw new DumpException("SAX Error parsing dump file", e);
        }
    }

}
