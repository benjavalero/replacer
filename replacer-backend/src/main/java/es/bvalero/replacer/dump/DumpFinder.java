package es.bvalero.replacer.dump;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.domain.ReplacerException;
import es.bvalero.replacer.domain.WikipediaLanguage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Find the latest Wikipedia dump in the filesystem where the application runs.
 * <p>
 * The dumps are generated monthly and placed in a shared folder in Wikipedia servers.
 * This dump base folder is structured in sub-folders corresponding to the different wiki-projects,
 * e.g. `eswiki`, which are also structured in sub-folders for each generation date, e.g. `20120120`,
 * containing finally the dump files.
 * <p>
 * This component looks, for one project, into the date folders to find the last
 * one containing a generated dump with the pages to be indexed. It is possible
 * that the last date folder exists but not all dumps are generated within it yet.
 * <p>
 * The path of the shared folder and the wiki-project are configured externally.
 */
@Slf4j
@Component
class DumpFinder {

    private static final String DUMP_FOLDER_REGEX = "\\d{8}";
    private static final Pattern DUMP_FOLDER_PATTERN = Pattern.compile(DUMP_FOLDER_REGEX);
    private static final String DUMP_PATH_PROJECT_FORMAT = "%swiki";
    private static final String DUMP_FILE_NAME_FORMAT = "%s-%s-pages-articles.xml.bz2";

    @Setter // For testing
    @Value("${replacer.dump.path.base:}")
    private String dumpPathBase;

    @Loggable(value = Loggable.DEBUG)
    Path findLatestDumpFile(WikipediaLanguage lang) throws ReplacerException {
        Path dumPath = Paths.get(dumpPathBase, getDumpPathProject(lang));
        LOGGER.trace("Dump path: {}", dumPath);

        Path latestDumpFile = null;
        for (Path dumpFolder : findDumpFolders(dumPath)) {
            Optional<Path> dumpFile = findDumpFile(dumpFolder, lang);
            if (dumpFile.isPresent()) {
                latestDumpFile = dumpFile.get();
                break;
            }
        }
        if (latestDumpFile == null) {
            throw new ReplacerException("No dump file has been found");
        }

        return latestDumpFile;
    }

    private List<Path> findDumpFolders(Path dumpPath) throws ReplacerException {
        try (Stream<Path> listedFiles = Files.list(Paths.get(dumpPath.toString()))) {
            // Return the dump folders reverse-ordered by name (date)
            return listedFiles
                .filter(folder -> DUMP_FOLDER_PATTERN.matcher(folder.getFileName().toString()).matches())
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ReplacerException("Error listing files in base folder", e);
        }
    }

    private Optional<Path> findDumpFile(Path dumpFolder, WikipediaLanguage lang) {
        // Check if the dump folder contains a valid dump
        String dumpFileName = String.format(DUMP_FILE_NAME_FORMAT, getDumpPathProject(lang), dumpFolder.getFileName());
        Path dumpFile = dumpFolder.resolve(dumpFileName);
        return dumpFile.toFile().exists() ? Optional.of(dumpFile) : Optional.empty();
    }

    private String getDumpPathProject(WikipediaLanguage lang) {
        return String.format(DUMP_PATH_PROJECT_FORMAT, lang.getCode());
    }
}
