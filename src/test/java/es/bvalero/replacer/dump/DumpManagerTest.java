package es.bvalero.replacer.dump;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
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

    private DumpManager dumpManager;

    @Before
    public void setUp() {
        dumpManager = Mockito.spy(new DumpManager());
        MockitoAnnotations.initMocks(this);
        Mockito.when(dumpManager.createDumpHandler(Mockito.anyBoolean())).thenReturn(dumpHandler);
    }

    @Test
    public void testFindLatestDumpFile() throws DumpException, IOException {
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
        // In case the latest sub-folder has not the dump yet (it has not been finished)
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
        dumpManager.setDumpFolderPath("");

        dumpManager.findLatestDumpFile();
    }

    @Test
    public void testParseDumpFile() throws URISyntaxException, DumpException {
        Path dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        Assert.assertNotNull(dumpFile);
        Assert.assertTrue(Files.exists(dumpFile));

        dumpManager.parseDumpFile(dumpFile);

        Mockito.verify(dumpHandler, Mockito.times(1)).startDocument();
    }

    @Test(expected = DumpException.class)
    public void testParseDumpFileNotExisting() throws DumpException {
        Path dumpFile = Paths.get("xxx");
        Assert.assertFalse(Files.exists(dumpFile));

        dumpManager.parseDumpFile(dumpFile);
    }

    @Test(expected = DumpException.class)
    public void testParseDumpFileWithParseException() throws URISyntaxException, DumpException {
        Path dumpFile = Paths.get(getClass().getResource("/false-positives.txt").toURI());
        Assert.assertNotNull(dumpFile);
        Assert.assertTrue(Files.exists(dumpFile));

        dumpManager.parseDumpFile(dumpFile);
    }

    @Test
    public void testParseDumpFileAlreadyRunning() throws URISyntaxException, DumpException {
        Path dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        Assert.assertNotNull(dumpFile);
        Assert.assertTrue(Files.exists(dumpFile));

        dumpManager.setRunning();
        dumpManager.parseDumpFile(dumpFile);

        Mockito.verify(dumpHandler, Mockito.times(0)).startDocument();
    }

    @Test
    public void testProcessNewDumpFile() throws URISyntaxException {
        Path dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        dumpManager.setDumpFolderPath(dumpFile.getParent().getParent().toString());

        Assert.assertNull(dumpManager.getLatestDumpFile());

        dumpManager.processLatestDumpFile(false, false);

        Mockito.verify(dumpHandler, Mockito.times(1)).startDocument();
        Assert.assertEquals(dumpFile, dumpManager.getLatestDumpFile());
    }

    @Test
    public void testProcessDumpFileAlreadyProcessed() throws URISyntaxException {
        Path dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        dumpManager.setDumpFolderPath(dumpFile.getParent().getParent().toString());
        dumpManager.setLatestDumpFile(dumpFile);

        dumpManager.processLatestDumpFile(false, false);

        Mockito.verify(dumpHandler, Mockito.times(0)).startDocument();
        Assert.assertEquals(dumpFile, dumpManager.getLatestDumpFile());
    }

    @Test
    public void testForceProcessDumpFileAlreadyProcessed() throws URISyntaxException {
        Path dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        dumpManager.setDumpFolderPath(dumpFile.getParent().getParent().toString());
        dumpManager.setLatestDumpFile(dumpFile);

        dumpManager.processLatestDumpFile(true, false);

        Mockito.verify(dumpHandler, Mockito.times(1)).startDocument();
        Assert.assertEquals(dumpFile, dumpManager.getLatestDumpFile());
    }

    @Test
    public void testProcessDefaultStatistics() {
        // Default statistics
        DumpProcessStatus defaultStats = dumpManager.getProcessStatus();
        Assert.assertFalse(defaultStats.isRunning());
        Assert.assertFalse(defaultStats.isForceProcess());
        Assert.assertEquals(0L, defaultStats.getNumArticlesRead());
        Assert.assertEquals(0L, defaultStats.getNumArticlesProcessed());
        Assert.assertEquals("-", defaultStats.getDumpFileName());
        Assert.assertEquals(0L, defaultStats.getAverage());
        Assert.assertEquals("0:00:00:00", defaultStats.getTime());
        Assert.assertEquals("0,00", defaultStats.getProgress());
    }

    @Test
    public void testProcessStatisticsWithMoreArticlesThanExpected() throws URISyntaxException {
        Path dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        dumpManager.setDumpFolderPath(dumpFile.getParent().getParent().toString());
        dumpManager.setNumArticlesEstimation(3L);
        Mockito.when(dumpHandler.getNumArticlesRead()).thenReturn(4L);
        Mockito.when(dumpHandler.getNumArticlesProcessed()).thenReturn(3L);

        dumpManager.processLatestDumpFile(false, false);

        // Statistics after processing
        DumpProcessStatus afterStats = dumpManager.getProcessStatus();
        Assert.assertFalse(afterStats.isRunning());
        Assert.assertFalse(afterStats.isForceProcess());
        Assert.assertEquals(4L, afterStats.getNumArticlesRead());
        Assert.assertEquals(3L, afterStats.getNumArticlesProcessed());
        Assert.assertEquals("eswiki-20170101-pages-articles.xml.bz2", afterStats.getDumpFileName());
        Assert.assertEquals("100,00", afterStats.getProgress());
    }

    @Test
    public void testProcessStatisticsWithLessArticlesThanExpected() throws URISyntaxException {
        Path dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        dumpManager.setDumpFolderPath(dumpFile.getParent().getParent().toString());
        dumpManager.setNumArticlesEstimation(5L);
        Mockito.when(dumpHandler.getNumArticlesRead()).thenReturn(4L);
        Mockito.when(dumpHandler.getNumArticlesProcessed()).thenReturn(3L);
        Mockito.when(dumpHandler.isForceProcess()).thenReturn(true);

        dumpManager.processLatestDumpFile(false, true);

        // Statistics after processing
        DumpProcessStatus afterStats = dumpManager.getProcessStatus();
        Assert.assertFalse(afterStats.isRunning());
        Assert.assertTrue(afterStats.isForceProcess());
        Assert.assertEquals(4L, afterStats.getNumArticlesRead());
        Assert.assertEquals(3L, afterStats.getNumArticlesProcessed());
        Assert.assertEquals("eswiki-20170101-pages-articles.xml.bz2", afterStats.getDumpFileName());
        Assert.assertEquals("100,00", afterStats.getProgress());
    }

}
