package es.bvalero.replacer.dump;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

/**
 * This component provides methods to find the most recent dump from Wikipedia.
 * The dump folder contains sub-folder names as the date of the dump, e. g. 20170820
 * The sub-folders contain several kinds of dumps, in particular, the one with all the
 * article contents, e. g. eswiki-20170820-pages-articles.xml.bz2
 */
@Component
class DumpFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DumpFinder.class);
    private static final String DATE_PATTERN = "yyyyMMdd";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);

    /**
     * @return The latest dump file, e. g.
     * /public/dumps/public/eswiki/20170820/eswiki-20170820-pages-articles.xml.bz2
     * @throws FileNotFoundException if the dump folder cannot be found.
     */
    @NotNull
    DumpFile findLatestDumpFile(@NotNull File dumpFolder) throws FileNotFoundException {
        DumpFile dumpFile = new DumpFile();

        File latestDumpFolder = findLatestDumpFolder(dumpFolder);

        // File path
        String dumpFileName = "eswiki-" + latestDumpFolder.getName() + "-pages-articles.xml.bz2";
        dumpFile.setFile(new File(latestDumpFolder, dumpFileName));

        // Date
        String dumpDateStr = latestDumpFolder.getName();
        try {
            dumpFile.setDate(dateFormat.parse(dumpDateStr));
        } catch (ParseException e) {
            LOGGER.error("Error parsing dump folder date: {}", dumpDateStr, e);
            throw new FileNotFoundException("Error parsing dump folder date: " + dumpDateStr);
        }

        return dumpFile;
    }

    /**
     * @return The path of the latest dump folder, e. g. /public/dumps/public/eswiki/20170820
     * @throws FileNotFoundException if the path contains no valid sub-folders
     */
    private File findLatestDumpFolder(@NotNull File dumpFolder) throws FileNotFoundException {
        File[] dumpSubFolders = dumpFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                // The sub-folders names are all numbers, e. g. 20170820
                return name.matches("[0-9]+");
            }
        });

        if (dumpSubFolders != null && dumpSubFolders.length > 0) {
            Arrays.sort(dumpSubFolders);
            return dumpSubFolders[dumpSubFolders.length - 1];
        } else {
            LOGGER.error("Sub-folders not found in dump: {}", dumpFolder);
            throw new FileNotFoundException("Sub-folders not found in dump: " + dumpFolder);
        }
    }

}
