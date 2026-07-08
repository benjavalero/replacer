package es.bvalero.replacer.dump;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Find the latest Wikipedia dump in the filesystem where the application runs.
 * The dumps are placed in a shared folder in Wikipedia servers with the structure:
 * {base}/{project}/{date}/xml/bzip2/{dump files}
 * e.g. /public/dumps/public/other/mediawiki_content_current/eswiki/2026-07-01/xml/bzip2/
 * There may be one or more dump files per date folder.
 */
@Slf4j
@Component
class DumpFinder {

    private static final String DUMP_FOLDER_REGEX = "\\d{4}-\\d{2}-\\d{2}";
    private static final Pattern DUMP_FOLDER_PATTERN = Pattern.compile(DUMP_FOLDER_REGEX);

    @Setter(onMethod_ = @TestOnly)
    @Value("${replacer.dump.path.base}")
    private String dumpPathBase;

    Collection<DumpFile> findLatestDumpFiles(WikipediaLanguage lang) {
        Path dumpPath = Paths.get(this.dumpPathBase, getDumpPathProject(lang));
        LOGGER.debug("Finding latest dump files in: {} ...", dumpPath);

        for (Path dumpFolder : findDumpFolders(dumpPath)) {
            Collection<DumpFile> dumpFiles = findDumpFiles(dumpFolder, lang);
            if (!dumpFiles.isEmpty()) {
                LOGGER.debug("Found latest dump files for {} Wikipedia: {}", lang, dumpFiles);
                return dumpFiles;
            }
        }

        LOGGER.error("No dump files found for {} Wikipedia", lang);
        return List.of();
    }

    private Collection<Path> findDumpFolders(Path dumpPath) {
        try (Stream<Path> listedFiles = Files.list(dumpPath)) {
            // Return the dump folders reverse-ordered by name (date)
            return listedFiles
                .filter(folder -> DUMP_FOLDER_PATTERN.matcher(folder.getFileName().toString()).matches())
                .sorted(Collections.reverseOrder())
                .toList();
        } catch (IOException e) {
            LOGGER.error("Error listing files in: {}", dumpPath, e);
            return List.of();
        }
    }

    private Collection<DumpFile> findDumpFiles(Path dumpFolder, WikipediaLanguage lang) {
        Path bzip2Folder = dumpFolder.resolve("xml/bzip2");
        if (!bzip2Folder.toFile().exists()) {
            return List.of();
        }

        String prefix = String.format("%s-%s-", getDumpPathProject(lang), dumpFolder.getFileName());
        try (Stream<Path> files = Files.list(bzip2Folder)) {
            return files
                .filter(f -> f.getFileName().toString().startsWith(prefix))
                .filter(f -> f.getFileName().toString().endsWith(".xml.bz2"))
                .map(DumpFile::of)
                .toList();
        } catch (IOException e) {
            LOGGER.error("Error listing files in: {}", bzip2Folder, e);
            return List.of();
        }
    }

    private String getDumpPathProject(WikipediaLanguage lang) {
        return String.format("%swiki", lang.getCode());
    }
}
