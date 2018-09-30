package es.bvalero.replacer.dump;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class DumpManagerTest {

    @Rule
    public TemporaryFolder dumpFolder = new TemporaryFolder();

    @Mock
    DumpHandler dumpHandler;

    private DumpManager dumpManager;

    @Before
    public void setUp() {
        dumpManager = Mockito.spy(new DumpManager());
        MockitoAnnotations.initMocks(this);
        Mockito.when(dumpManager.createDumpHandler(Mockito.anyBoolean())).thenReturn(dumpHandler);
    }

    @Test
    public void testFindLatestDumpFile() throws DumpException, IOException {
        File subFolder1 = dumpFolder.newFolder("20170101");
        File subFolder2 = dumpFolder.newFolder("20170201");
        File dumpFile1 = new File(subFolder1, "eswiki-" + subFolder1.getName() + "-pages-articles.xml.bz2");
        File dumpFile2 = new File(subFolder2, "eswiki-" + subFolder2.getName() + "-pages-articles.xml.bz2");
        Assert.assertTrue(dumpFile1.createNewFile());
        Assert.assertTrue(dumpFile2.createNewFile());
        dumpManager.setDumpFolderPath(dumpFolder.getRoot().getPath());

        File latestDumpFile = dumpManager.findLatestDumpFile();

        Assert.assertNotNull(latestDumpFile);
        Assert.assertEquals(dumpFile2, latestDumpFile);
    }

    @Test
    public void testFindLatestDumpFileInOldSubFolder() throws DumpException, IOException {
        // In case the latest sub-folder has not the dump yet (it has not been finished)
        File subFolder1 = dumpFolder.newFolder("20170101");
        dumpFolder.newFolder("20170201");
        File dumpFile1 = new File(subFolder1, "eswiki-" + subFolder1.getName() + "-pages-articles.xml.bz2");
        Assert.assertTrue(dumpFile1.createNewFile());
        dumpManager.setDumpFolderPath(dumpFolder.getRoot().getPath());

        File latestDumpFile = dumpManager.findLatestDumpFile();

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
        File dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI()).toFile();
        Assert.assertNotNull(dumpFile);
        Assert.assertTrue(dumpFile.exists());

        dumpManager.parseDumpFile(dumpFile);

        Mockito.verify(dumpHandler, Mockito.times(1)).startDocument();
    }

    @Test(expected = DumpException.class)
    public void testParseDumpFileNotExisting() throws DumpException {
        File dumpFile = new File("");
        Assert.assertFalse(dumpFile.exists());

        dumpManager.parseDumpFile(dumpFile);
    }

    @Test(expected = DumpException.class)
    public void testParseDumpFileWithParseException() throws URISyntaxException, DumpException {
        File dumpFile = Paths.get(getClass().getResource("/false-positives.txt").toURI()).toFile();
        Assert.assertNotNull(dumpFile);
        Assert.assertTrue(dumpFile.exists());

        dumpManager.parseDumpFile(dumpFile);
    }

    @Test
    public void testParseDumpFileAlreadyRunning() throws URISyntaxException, DumpException {
        File dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI()).toFile();
        Assert.assertNotNull(dumpFile);
        Assert.assertTrue(dumpFile.exists());

        dumpManager.setRunning();
        dumpManager.parseDumpFile(dumpFile);

        Mockito.verify(dumpHandler, Mockito.times(0)).startDocument();
    }

    @Test
    public void testProcessNewDumpFile() throws URISyntaxException {
        File dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI()).toFile();
        dumpManager.setDumpFolderPath(dumpFile.getParentFile().getParentFile().getPath());

        Assert.assertEquals("-", dumpManager.getLatestDumpFile());

        dumpManager.processLatestDumpFile(false, false);

        Mockito.verify(dumpHandler, Mockito.times(1)).startDocument();
        Assert.assertEquals(dumpFile.getPath(), dumpManager.getLatestDumpFile());
    }

    @Test
    public void testProcessDumpFileAlreadyProcessed() throws URISyntaxException {
        File dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI()).toFile();
        dumpManager.setDumpFolderPath(dumpFile.getParentFile().getParentFile().getPath());
        dumpManager.setLatestDumpFile(dumpFile.getPath());

        dumpManager.processLatestDumpFile(false, false);

        Mockito.verify(dumpHandler, Mockito.times(0)).startDocument();
        Assert.assertEquals(dumpFile.getPath(), dumpManager.getLatestDumpFile());
    }

    @Test
    public void testForceProcessDumpFileAlreadyProcessed() throws URISyntaxException {
        File dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI()).toFile();
        dumpManager.setDumpFolderPath(dumpFile.getParentFile().getParentFile().getPath());
        dumpManager.setLatestDumpFile(dumpFile.getPath());

        dumpManager.processLatestDumpFile(true, false);

        Mockito.verify(dumpHandler, Mockito.times(1)).startDocument();
        Assert.assertEquals(dumpFile.getPath(), dumpManager.getLatestDumpFile());
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
        File dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI()).toFile();
        dumpManager.setDumpFolderPath(dumpFile.getParentFile().getParentFile().getPath());
        dumpManager.setNumArticlesEstimation(3);
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
        File dumpFile = Paths.get(getClass().getResource("/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI()).toFile();
        dumpManager.setDumpFolderPath(dumpFile.getParentFile().getParentFile().getPath());
        dumpManager.setNumArticlesEstimation(5);
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
