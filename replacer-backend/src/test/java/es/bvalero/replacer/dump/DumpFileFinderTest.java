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
    public TemporaryFolder dumpFolder = new TemporaryFolder();

    private DumpFileFinder dumpFileFinder;

    @Before
    public void setUp() {
        dumpFileFinder = new DumpFileFinder();
    }

    @Test
    public void testFindLatestDumpFile() throws DumpException, IOException {
        // Two folders: 1 and 2 (newer). Each with one dump file.
        Path dumpFolderFile = Paths.get(dumpFolder.getRoot().toURI());
        Path subFolder1 = dumpFolderFile.resolve("20170101");
        Path subFolder2 = dumpFolderFile.resolve("20170201");
        Files.createDirectory(subFolder1);
        Files.createDirectory(subFolder2);
        Path dumpFile1 = subFolder1.resolve(String.format(DumpFileFinder.DUMP_FILE_NAME_FORMAT, subFolder1.getFileName()));
        Path dumpFile2 = subFolder2.resolve(String.format(DumpFileFinder.DUMP_FILE_NAME_FORMAT, subFolder2.getFileName()));
        Files.createFile(dumpFile1);
        Files.createFile(dumpFile2);
        dumpFileFinder.setDumpBaseFolder(dumpFolder.getRoot().getPath());

        Path latestDumpFile = dumpFileFinder.findLatestDumpFile();

        Assert.assertNotNull(latestDumpFile);
        Assert.assertEquals(dumpFile2, latestDumpFile);
    }

    @Test
    public void testFindLatestDumpFileInOldSubFolder() throws DumpException, IOException {
        // In case the latest sub-folder has not the dump yet (it has not been finished)
        Path dumpFolderFile = Paths.get(dumpFolder.getRoot().toURI());
        Path subFolder1 = dumpFolderFile.resolve("20170101");
        Path subFolder2 = dumpFolderFile.resolve("20170201");
        Files.createDirectory(subFolder1);
        Files.createDirectory(subFolder2);
        Path dumpFile1 = subFolder1.resolve(String.format(DumpFileFinder.DUMP_FILE_NAME_FORMAT, subFolder1.getFileName()));
        Files.createFile(dumpFile1);
        dumpFileFinder.setDumpBaseFolder(dumpFolder.getRoot().getPath());

        Path latestDumpFile = dumpFileFinder.findLatestDumpFile();

        Assert.assertNotNull(latestDumpFile);
        Assert.assertEquals(dumpFile1, latestDumpFile);
    }

    @Test(expected = DumpException.class)
    public void testFindLatestDumpFileWithoutDumpFiles() throws DumpException, IOException {
        // In case there is no sub-folder with a dump yet (it has not been finished)
        dumpFolder.newFolder("20170101");
        dumpFileFinder.setDumpBaseFolder(dumpFolder.getRoot().getPath());

        dumpFileFinder.findLatestDumpFile();
    }

    @Test(expected = DumpException.class)
    public void testFindLatestDumpFileWithoutSubFolders() throws DumpException {
        dumpFileFinder.setDumpBaseFolder(dumpFolder.getRoot().getPath());

        dumpFileFinder.findLatestDumpFile();
    }

    @Test(expected = DumpException.class)
    public void testFindLatestDumpFileWithNotExistingDumpPath() throws DumpException {
        dumpFileFinder.setDumpBaseFolder("xxx");

        dumpFileFinder.findLatestDumpFile();
    }

}
