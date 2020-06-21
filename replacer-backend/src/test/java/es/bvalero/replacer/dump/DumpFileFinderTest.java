package es.bvalero.replacer.dump;

import es.bvalero.replacer.ReplacerException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

// TODO: Adapt to Junit5
public class DumpFileFinderTest {
    @Rule
    public TemporaryFolder dumpBaseFolder = new TemporaryFolder();

    private DumpFinder dumpFinder;

    @Before
    public void setUp() {
        dumpFinder = new DumpFinder();
    }

    @Test
    public void testFindLatestDumpFile() throws IOException, ReplacerException {
        // Two folders: 1 (old) and 2 (new). Each with one valid dump file.
        // The newer also contains a non-valid dump file.
        String dumpPathBase = dumpBaseFolder.getRoot().getPath();
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

        Path latestDumpFile = dumpFinder.findLatestDumpFile(WikipediaLanguage.SPANISH);

        Assert.assertNotNull(latestDumpFile);
        Assert.assertEquals(dumpFile2, latestDumpFile);
    }

    @Test
    public void testFindLatestDumpFileInOldSubFolder() throws IOException, ReplacerException {
        // In case the latest dump folder has not a valid dump yet
        // (the generation is not done yet)
        String dumpPathBase = dumpBaseFolder.getRoot().getPath();
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

        Path latestDumpFile = dumpFinder.findLatestDumpFile(WikipediaLanguage.SPANISH);

        Assert.assertNotNull(latestDumpFile);
        Assert.assertEquals(dumpFile1, latestDumpFile);
    }

    @Test(expected = ReplacerException.class)
    public void testEmptyDumpFolders() throws IOException, ReplacerException {
        // In case there is no dump folder with a dump yet
        String dumpPathBase = dumpBaseFolder.getRoot().getPath();
        String dumpPathProject = "eswiki";

        Path dumpBase = Paths.get(dumpPathBase);
        Path dumpProject = dumpBase.resolve(dumpPathProject);
        Files.createDirectory(dumpProject);
        Path subFolder1 = dumpProject.resolve("20170101");
        Files.createDirectory(subFolder1);

        dumpFinder.setDumpPathBase(dumpPathBase);

        dumpFinder.findLatestDumpFile(WikipediaLanguage.SPANISH);
    }

    @Test(expected = ReplacerException.class)
    public void testDumpFolderWithoutValidSubFolders() throws IOException, ReplacerException {
        String dumpPathBase = dumpBaseFolder.getRoot().getPath();
        String dumpPathProject = "eswiki";

        Path dumpBase = Paths.get(dumpPathBase);
        Path dumpProject = dumpBase.resolve(dumpPathProject);
        Files.createDirectory(dumpProject);
        Path subFolder1 = dumpProject.resolve("20170101-temp");
        Files.createDirectory(subFolder1);
        Path dumpFile1 = subFolder1.resolve("eswiki-20170101-pages-articles.xml.bz2");
        Files.createFile(dumpFile1);

        dumpFinder.setDumpPathBase(dumpPathBase);

        dumpFinder.findLatestDumpFile(WikipediaLanguage.SPANISH);
    }

    @Test(expected = ReplacerException.class)
    public void testNotExistingDumpProjectFolder() throws ReplacerException {
        dumpFinder.setDumpPathBase(dumpBaseFolder.getRoot().getPath());

        // Don't create the project sub-folder for the test
        dumpFinder.findLatestDumpFile(WikipediaLanguage.SPANISH);
    }

    @Test(expected = ReplacerException.class)
    public void testNotExistingDumpBaseFolder() throws ReplacerException {
        dumpFinder.setDumpPathBase("xxx");

        dumpFinder.findLatestDumpFile(WikipediaLanguage.SPANISH);
    }
}
