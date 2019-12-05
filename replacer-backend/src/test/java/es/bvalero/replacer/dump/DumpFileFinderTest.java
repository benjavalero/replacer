package es.bvalero.replacer.dump;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DumpFileFinderTest {

    @Rule
    public TemporaryFolder dumpBaseFolder = new TemporaryFolder();

    private DumpFileFinder dumpFileFinder;

    @Before
    public void setUp() {
        dumpFileFinder = new DumpFileFinder();
    }

    @Test
    public void testFindLatestDumpFile() throws IOException, DumpException {
        // Two folders: 1 (old) and 2 (new). Each with one valid dump file.
        // The newer contains a non-valid dump file.
        Path dumpBaseFolderFile = Paths.get(dumpBaseFolder.getRoot().toURI());
        Path subFolder1 = dumpBaseFolderFile.resolve("20170101");
        Path subFolder2 = dumpBaseFolderFile.resolve("20170201");
        Files.createDirectory(subFolder1);
        Files.createDirectory(subFolder2);
        Path dumpFile1 = subFolder1.resolve("eswiki-20170101-pages-articles.xml.bz2");
        Path dumpFile2 = subFolder2.resolve("eswiki-20170201-pages-articles.xml.bz2");
        Path dumpFile3 = subFolder2.resolve("eswiki-20190201-pages-meta-current.xml.bz2"); // Non-valid
        Files.createFile(dumpFile1);
        Files.createFile(dumpFile2);
        Files.createFile(dumpFile3);
        dumpFileFinder.setDumpBaseFolder(dumpBaseFolderFile.toString());

        Path latestDumpFile = dumpFileFinder.findLatestDumpFile();

        Assert.assertNotNull(latestDumpFile);
        Assert.assertEquals(dumpFile2, latestDumpFile);
    }

    @Test
    public void testFindLatestDumpFileInOldSubFolder() throws IOException, DumpException {
        // In case the latest dump folder has not a valid dump yet
        // (the generation is not done yet)
        Path dumpBaseFolderFile = Paths.get(dumpBaseFolder.getRoot().toURI());
        Path subFolder1 = dumpBaseFolderFile.resolve("20170101");
        Path subFolder2 = dumpBaseFolderFile.resolve("20170201");
        Files.createDirectory(subFolder1);
        Files.createDirectory(subFolder2);
        Path dumpFile1 = subFolder1.resolve("eswiki-20170101-pages-articles.xml.bz2");
        Files.createFile(dumpFile1);
        dumpFileFinder.setDumpBaseFolder(dumpBaseFolderFile.toString());

        Path latestDumpFile = dumpFileFinder.findLatestDumpFile();

        Assert.assertNotNull(latestDumpFile);
        Assert.assertEquals(dumpFile1, latestDumpFile);
    }

    @Test(expected = DumpException.class)
    public void testNotExistingValidDumpFolder() throws IOException, DumpException {
        // In case there is no dump folder with a dump yet
        dumpBaseFolder.newFolder("20170101");
        dumpFileFinder.setDumpBaseFolder(dumpBaseFolder.getRoot().getPath());

        dumpFileFinder.findLatestDumpFile();
    }

    @Test(expected = DumpException.class)
    public void testDumpBaseFolderWithoutValidSubFolders() throws IOException, DumpException {
        dumpBaseFolder.newFolder("20170101-temp");
        dumpFileFinder.setDumpBaseFolder(dumpBaseFolder.getRoot().getPath());

        dumpFileFinder.findLatestDumpFile();
    }

    @Test(expected = DumpException.class)
    public void testNotExistingDumpBaseFolder() throws DumpException {
        dumpFileFinder.setDumpBaseFolder("xxx");

        dumpFileFinder.findLatestDumpFile();
    }

}
