package es.bvalero.replacer.dump;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DumpManagerTest {

    @Rule
    public TemporaryFolder dumpFolder = new TemporaryFolder();

    @Mock
    private DumpHandler dumpHandler;

    @InjectMocks
    private DumpManager dumpManager;

    @Before
    public void setUp() {
        dumpManager = new DumpManager();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindLatestDumpFile() throws DumpException, IOException {
        // Two folders: 1 and 2 (newer). Each with one dump file.
        Path dumpFolderFile = Paths.get(dumpFolder.getRoot().toURI());
        Path subFolder1 = dumpFolderFile.resolve("20170101");
        Path subFolder2 = dumpFolderFile.resolve("20170201");
        Files.createDirectory(subFolder1);
        Files.createDirectory(subFolder2);
        Path dumpFile1 = subFolder1.resolve(String.format(DumpManager.DUMP_NAME_FORMAT, subFolder1.getFileName()));
        Path dumpFile2 = subFolder2.resolve(String.format(DumpManager.DUMP_NAME_FORMAT, subFolder2.getFileName()));
        Files.createFile(dumpFile1);
        Files.createFile(dumpFile2);
        dumpManager.setDumpFolderPath(dumpFolder.getRoot().getPath());

        Path latestDumpFile = dumpManager.findLatestDumpFile();

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
        Path dumpFile1 = subFolder1.resolve(String.format(DumpManager.DUMP_NAME_FORMAT, subFolder1.getFileName()));
        Files.createFile(dumpFile1);
        dumpManager.setDumpFolderPath(dumpFolder.getRoot().getPath());

        Path latestDumpFile = dumpManager.findLatestDumpFile();

        Assert.assertNotNull(latestDumpFile);
        Assert.assertEquals(dumpFile1, latestDumpFile);
    }

    @Test(expected = DumpException.class)
    public void testFindLatestDumpFileWithoutDumpFiles() throws DumpException, IOException {
        // In case there is no sub-folder with a dump yet (it has not been finished)
        dumpFolder.newFolder("20170101");
        dumpManager.setDumpFolderPath(dumpFolder.getRoot().getPath());

        dumpManager.findLatestDumpFile();
    }

    @Test(expected = DumpException.class)
    public void testFindLatestDumpFileWithoutSubFolders() throws DumpException {
        dumpManager.setDumpFolderPath(dumpFolder.getRoot().getPath());

        dumpManager.findLatestDumpFile();
    }

    @Test(expected = DumpException.class)
    public void testFindLatestDumpFileWithNotExistingDumpPath() throws DumpException {
        dumpManager.setDumpFolderPath("xxx");

        dumpManager.findLatestDumpFile();
    }

    @Test
    public void testParseDumpFile() throws URISyntaxException, DumpException {
        // We need a real dump file to create the input stream
        Path dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        Assert.assertNotNull(dumpFile);
        Assert.assertTrue(Files.exists(dumpFile));

        dumpManager.parseDumpFile(dumpFile, true);

        Mockito.verify(dumpHandler, Mockito.times(1)).setLatestDumpFile(dumpFile);
        Mockito.verify(dumpHandler, Mockito.times(1)).setForceProcess(true);
        Mockito.verify(dumpHandler, Mockito.times(1)).startDocument();
    }

    @Test(expected = DumpException.class)
    public void testParseDumpFileNotExisting() throws DumpException {
        Path dumpFile = Paths.get("xxx");
        Assert.assertFalse(Files.exists(dumpFile));

        dumpManager.parseDumpFile(dumpFile, false);
    }

    @Test(expected = DumpException.class)
    public void testParseDumpFileWithParseException() throws URISyntaxException, DumpException {
        Path dumpFile = Paths.get(getClass().getResource("/non-valid-dump.txt.bz2").toURI());
        Assert.assertNotNull(dumpFile);
        Assert.assertTrue(Files.exists(dumpFile));

        dumpManager.parseDumpFile(dumpFile, false);
    }

    @Test
    public void testProcessLatestDumpFile() throws URISyntaxException {
        Path dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        dumpManager.setDumpFolderPath(dumpFile.getParent().getParent().toString());

        dumpManager.processLatestDumpFile(false);

        Mockito.verify(dumpHandler, Mockito.times(1)).startDocument();
    }

    @Test
    public void testProcessLatestDumpFileWithException() {
        dumpManager.setDumpFolderPath("");

        dumpManager.processLatestDumpFile(false);

        Mockito.verify(dumpHandler, Mockito.times(0)).startDocument();
    }

    @Test
    public void testProcessLatestDumpFileAlreadyRunning() {
        Mockito.when(dumpHandler.isRunning()).thenReturn(true);

        dumpManager.processLatestDumpFile(false);

        Mockito.verify(dumpHandler, Mockito.times(0)).startDocument();
    }

    @Test
    public void testProcessLatestDumpFileAlreadyProcessed() throws URISyntaxException {
        Path dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        dumpManager.setDumpFolderPath(dumpFile.getParent().getParent().toString());
        Mockito.when(dumpHandler.getLatestDumpFile()).thenReturn(dumpFile);

        dumpManager.processLatestDumpFile(false);

        Mockito.verify(dumpHandler, Mockito.times(0)).startDocument();
    }

    @Test
    public void testProcessLatestDumpFileAlreadyProcessedForced() throws URISyntaxException {
        Path dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        dumpManager.setDumpFolderPath(dumpFile.getParent().getParent().toString());
        Mockito.when(dumpHandler.getLatestDumpFile()).thenReturn(dumpFile);

        dumpManager.processLatestDumpFile(true);

        Mockito.verify(dumpHandler, Mockito.times(1)).startDocument();
    }

    @Test
    public void testProcessDumpScheduled() throws URISyntaxException {
        Path dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        dumpManager.setDumpFolderPath(dumpFile.getParent().getParent().toString());

        dumpManager.processDumpScheduled();

        Mockito.verify(dumpHandler, Mockito.times(1)).startDocument();
    }

}
