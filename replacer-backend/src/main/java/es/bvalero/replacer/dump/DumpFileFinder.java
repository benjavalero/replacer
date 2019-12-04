package es.bvalero.replacer.dump;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Find the latest Wikipedia dump in the filesystem where the application runs.
 */
@Slf4j
@Component
class DumpFileFinder {
    static final String DUMP_FILE_NAME_FORMAT = "eswiki-%s-pages-articles.xml.bz2";
    private static final String DUMP_FOLDER_NAME_REGEX = "\\d{8}";
    private static final Pattern DUMP_FOLDER_NAME_PATTERN = Pattern.compile(DUMP_FOLDER_NAME_REGEX);

    @Setter
    @Value("${replacer.dump.folder.path:}")
    private String dumpBaseFolder;

    Path findLatestDumpFile() throws DumpException {
        LOGGER.info("START Find latest dump file in base folder: {}", dumpBaseFolder);
        for (Path dumpFolder : findDumpFolders()) {
            // Check if the dump folder contains a valid dump
            String dumpFileName = String.format(DUMP_FILE_NAME_FORMAT, dumpFolder.getFileName());
            Path dumpFile = dumpFolder.resolve(dumpFileName);
            if (dumpFile.toFile().exists()) {
                LOGGER.info("END Find latest dump file: {}", dumpFile);
                return dumpFile;
            }
        }

        // If we get here no dump file has been found
        throw new DumpException("No dump file has been found");
    }

    // Return the dump folders reverse-ordered by name (date)
    private List<Path> findDumpFolders() throws DumpException {
        LOGGER.debug("START Find dump folders in base folder: {}", dumpBaseFolder);
        try (Stream<Path> listedFiles = Files.list(Paths.get(dumpBaseFolder))) {
            List<Path> dumpFolders = listedFiles
                    .filter(folder -> DUMP_FOLDER_NAME_PATTERN.matcher(folder.getFileName().toString()).matches())
                    .sorted(Collections.reverseOrder())
                    .collect(Collectors.toList());
            if (dumpFolders.isEmpty()) {
                throw new DumpException("No dump folders found");
            }
            LOGGER.debug("Dump folders found: {}", dumpFolders.size());
            return dumpFolders;
        } catch (IOException e) {
            throw new DumpException("Error listing files in base folder", e);
        }
    }

}
