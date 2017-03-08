package es.bvalero.replacer.task;

import es.bvalero.replacer.parse.ArticlesParser;
import es.bvalero.replacer.parse.FindMisspellingsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

@Component
class UpdateReplacementsTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateReplacementsTask.class);

    @Autowired
    private ArticlesParser articlesParser;

    @Value("${replacer.xml.path}")
    private String dumpFolder;

    @Scheduled(cron = "0 0 5 1 1/1 ?")
    void updateReplacements() {
        String dumpPath = findLatestDumpPath(new File(dumpFolder));

        LOGGER.info("Parsing dump file: {}", dumpPath);
        FindMisspellingsHandler handler = new FindMisspellingsHandler();
        boolean success = articlesParser.parse(dumpPath, handler);
        LOGGER.info("Parse completed with success: {}", success);
    }

    String findLatestDumpPath(File dumpFolderFile) {
        File[] folders = dumpFolderFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches("[0-9]+");
            }
        });
        Arrays.sort(folders);
        File dumpFolder = folders[folders.length - 1];
        String dumpFileName = "eswiki-" + dumpFolder.getName() + "-pages-articles.xml.bz2";
        return new File(dumpFolder, dumpFileName).getPath();
    }

}
