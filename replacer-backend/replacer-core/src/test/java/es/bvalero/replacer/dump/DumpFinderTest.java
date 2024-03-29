package es.bvalero.replacer.dump;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DumpFinderTest {

    @TempDir
    Path dumpBaseFolder;

    private DumpFinder dumpFinder;

    @BeforeEach
    public void setUp() {
        dumpFinder = new DumpFinder();
    }

    @Test
    void testFindLatestDumpFile() throws Exception {
        // Two folders: 1 (old) and 2 (new). Each with one valid dump file.
        // The newer also contains a non-valid dump file.
        String dumpPathBase = dumpBaseFolder.toString();
        String dumpPathProject = "eswiki";

        Path dumpBase = Paths.get(dumpPathBase);
        Path dumpProject = dumpBase.resolve(dumpPathProject);
        Files.createDirectory(dumpProject);
        Path subFolder1 = dumpProject.resolve("20170101");
        Path subFolder2 = dumpProject.resolve("20170201");
        Files.createDirectory(subFolder1);
        Files.createDirectory(subFolder2);
        Path dumpFile1 = subFolder1.resolve("eswiki-20170101-pages-articles.xml.bz2");
        Path dumpFile2 = subFolder2.resolve("eswiki-20170201-pages-articles.xml.bz2");
        Path dumpFile3 = subFolder2.resolve("eswiki-20190201-pages-meta-current.xml.bz2"); // Non-valid
        Files.createFile(dumpFile1);
        Files.createFile(dumpFile2);
        Files.createFile(dumpFile3);

        dumpFinder.setDumpPathBase(dumpPathBase);

        Optional<DumpFile> latestDumpFile = dumpFinder.findLatestDumpFile(WikipediaLanguage.SPANISH);

        assertTrue(latestDumpFile.isPresent());
        assertEquals(dumpFile2, latestDumpFile.get().getPath());
    }

    @Test
    void testFindLatestDumpFileInOldSubFolder() throws Exception {
        // In case the latest dump folder has not a valid dump yet
        // (the generation is not done yet)
        String dumpPathBase = dumpBaseFolder.toString();
        String dumpPathProject = "eswiki";

        Path dumpBase = Paths.get(dumpPathBase);
        Path dumpProject = dumpBase.resolve(dumpPathProject);
        Files.createDirectory(dumpProject);
        Path subFolder1 = dumpProject.resolve("20170101");
        Path subFolder2 = dumpProject.resolve("20170201");
        Files.createDirectory(subFolder1);
        Files.createDirectory(subFolder2);
        Path dumpFile1 = subFolder1.resolve("eswiki-20170101-pages-articles.xml.bz2");
        Files.createFile(dumpFile1);

        dumpFinder.setDumpPathBase(dumpPathBase);

        Optional<DumpFile> latestDumpFile = dumpFinder.findLatestDumpFile(WikipediaLanguage.SPANISH);

        assertTrue(latestDumpFile.isPresent());
        assertEquals(dumpFile1, latestDumpFile.get().getPath());
    }

    @Test
    void testEmptyDumpFolders() throws Exception {
        // In case there is no dump folder with a dump yet
        String dumpPathBase = dumpBaseFolder.toString();
        String dumpPathProject = "eswiki";

        Path dumpBase = Paths.get(dumpPathBase);
        Path dumpProject = dumpBase.resolve(dumpPathProject);
        Files.createDirectory(dumpProject);
        Path subFolder1 = dumpProject.resolve("20170101");
        Files.createDirectory(subFolder1);

        dumpFinder.setDumpPathBase(dumpPathBase);

        assertTrue(dumpFinder.findLatestDumpFile(WikipediaLanguage.SPANISH).isEmpty());
    }

    @Test
    void testDumpFolderWithoutValidSubFolders() throws Exception {
        String dumpPathBase = dumpBaseFolder.toString();
        String dumpPathProject = "eswiki";

        Path dumpBase = Paths.get(dumpPathBase);
        Path dumpProject = dumpBase.resolve(dumpPathProject);
        Files.createDirectory(dumpProject);
        Path subFolder1 = dumpProject.resolve("20170101-temp");
        Files.createDirectory(subFolder1);
        Path dumpFile1 = subFolder1.resolve("eswiki-20170101-pages-articles.xml.bz2");
        Files.createFile(dumpFile1);

        dumpFinder.setDumpPathBase(dumpPathBase);

        assertTrue(dumpFinder.findLatestDumpFile(WikipediaLanguage.SPANISH).isEmpty());
    }

    @Test
    void testNotExistingDumpProjectFolder() {
        dumpFinder.setDumpPathBase(dumpBaseFolder.toString());

        // Don't create the project sub-folder for the test
        assertTrue(dumpFinder.findLatestDumpFile(WikipediaLanguage.SPANISH).isEmpty());
    }

    @Test
    void testNotExistingDumpBaseFolder() {
        dumpFinder.setDumpPathBase("xxx");

        assertTrue(dumpFinder.findLatestDumpFile(WikipediaLanguage.SPANISH).isEmpty());
    }
}
