package es.bvalero.replacer.dump;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.jetbrains.annotations.TestOnly;
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
@Slf4j
@Component
class DumpManager {

    static final String DUMP_NAME_FORMAT = "eswiki-%s-pages-articles.xml.bz2";
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
     * Check daily if there is a new dump to process (with a one-hour delay)
     */
    @Scheduled(fixedDelay = 3600 * 24 * 1000, initialDelay = 3600 * 1000)
    void processDumpScheduled() {
        LOGGER.info("EXECUTE Scheduled weekly index of the last dump");
        processLatestDumpFile(false);
    }

    /**
     * Find the latest dump file and process it.
     *
     * @param forceProcessDumpArticles Force processing all dump articles event if they have not been modified since
     *                                 last processing. Also force processing again an already indexed dump.
     */
    // In order to be asynchronous it must be public and called externally:
    // https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/annotation/EnableAsync.html
    @SuppressWarnings("WeakerAccess")
    @Async
    public void processLatestDumpFile(boolean forceProcessDumpArticles) {
        LOGGER.info("START Indexation of latest dump file. Force: {}", forceProcessDumpArticles);

        // Check just in case the handler is already running
        if (dumpHandler.isRunning()) {
            LOGGER.info("END Indexation of latest dump file. Dump indexation is already running.");
            return;
        }

        try {
            Path latestDumpFileFound = findLatestDumpFile();

            // We check against the latest dump file processed
            if (!latestDumpFileFound.equals(dumpHandler.getLatestDumpFile()) || forceProcessDumpArticles) {
                parseDumpFile(latestDumpFileFound, forceProcessDumpArticles);
                LOGGER.info("END Indexation of latest dump file: {}", latestDumpFileFound);
            } else {
                LOGGER.info("END Indexation of latest dump file. Latest dump file already indexed.");
            }
        } catch (DumpException e) {
            LOGGER.error("Error indexing latest dump file", e);
        }
    }

    /**
     * @return The path of latest available dump file from Wikipedia.
     */
    Path findLatestDumpFile() throws DumpException {
        LOGGER.info("START Find latest dump file");
        List<Path> dumpSubFolders = findDumpFolders();

        // Try with all the folders starting for the latest
        for (Path dumpSubFolder : dumpSubFolders) {
            String dumpFileName = String.format(DUMP_NAME_FORMAT, dumpSubFolder.getFileName());
            Path dumpFile = dumpSubFolder.resolve(dumpFileName);
            if (dumpFile.toFile().exists()) {
                LOGGER.info("END Find latest dump file: {}", dumpFile);
                return dumpFile;
            }
        }

        // If we get here no dump file has been found
        throw new DumpException("No dump file has been found in dump path");
    }

    private List<Path> findDumpFolders() throws DumpException {
        LOGGER.debug("Dump files base path: {}", dumpFolderPath);
        try (Stream<Path> dumpSubPaths = Files.list(Paths.get(dumpFolderPath))) {
            List<Path> dumpSubFolders = dumpSubPaths
                    .filter(folder -> PATTERN_DUMP_FOLDER.matcher(folder.getFileName().toString()).matches())
                    .sorted(Collections.reverseOrder())
                    .collect(Collectors.toList());
            if (dumpSubFolders.isEmpty()) {
                throw new DumpException(String.format("Sub-folders not found in dump path: %s", dumpFolderPath));
            }
            return dumpSubFolders;
        } catch (IOException e) {
            throw new DumpException("Error listing files in dump path", e);
        }
    }

    void parseDumpFile(Path dumpFile, boolean forceProcess) throws DumpException {
        LOGGER.info("START Parse dump file: {}", dumpFile);

        try (InputStream xmlInput = new BZip2CompressorInputStream(Files.newInputStream(dumpFile))) {
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

    DumpProcessStatus getDumpStatus() {
        return dumpHandler.getProcessStatus();
    }

}
