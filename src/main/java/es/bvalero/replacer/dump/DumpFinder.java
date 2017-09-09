package es.bvalero.replacer.dump;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * This component provides methods to find the most recent dump from Wikipedia.
 * The dump folder contains subfolders name as the date of the dump, e. g. 20170820
 * The subfolders contains several kinds of dumps, in particular, the one with all the article contents,
 * e. g. eswiki-20170820-pages-articles.xml.bz2
 */
@Component
class DumpFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DumpFinder.class);

    @Value("${replacer.dump.folder.path:}")
    private String dumpFolderPath;

    private File dumpFolder;

    private File getDumpFolder() {
        if (this.dumpFolder == null) {
            this.dumpFolder = new File(this.dumpFolderPath);
        }
        return this.dumpFolder;
    }

    void setDumpFolder(File dumpFolder) {
        this.dumpFolder = dumpFolder;
    }

    /**
     * @return Returns the path of the latest dump folder,  e. g.
     * /public/dumps/public/eswiki/20170820
     * @throws FileNotFoundException if the path contains no valid sub-folders
     */
    private File findLatestDumpFolder() throws FileNotFoundException {
        File dumpFolder = getDumpFolder();
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

    /**
     * Finds the date of the latest dump, or null in case of error or no folder found.
     */
    Date findLatestDumpDate() throws FileNotFoundException {
        File latestDumpFolder = findLatestDumpFolder();
        String dumpDateStr = latestDumpFolder.getName();
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            return formatter.parse(dumpDateStr);
        } catch (ParseException e) {
            LOGGER.error("Error parsing dump folder date: {}", dumpDateStr, e);
            throw new FileNotFoundException("Error parsing dump folder date: " + dumpDateStr);
        }
    }

    /**
     * @return The path of the latest dump file, e. g.
     * /public/dumps/public/eswiki/20170820/eswiki-20170820-pages-articles.xml.bz2
     * @throws FileNotFoundException if the dump folder cannot be found.
     */
    File findLatestDumpFile() throws FileNotFoundException {
        File latestDumpFolder = findLatestDumpFolder();
        String dumpFileName = "eswiki-" + latestDumpFolder.getName() + "-pages-articles.xml.bz2";
        return new File(latestDumpFolder, dumpFileName);
    }

}
