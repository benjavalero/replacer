package es.bvalero.replacer.dump;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import es.bvalero.replacer.ReplacerException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DumpManagerTest {

    @Mock
    public DumpFileFinder dumpFileFinder;

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
    public void testParseDumpFile() throws URISyntaxException, DumpException {
        // We need a real dump file to create the input stream
        Path dumpFile = Paths.get(getClass().getResource("/es/bvalero/replacer/dump/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
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
        Path dumpFile = Paths.get(getClass().getResource("/es/bvalero/replacer/dump/non-valid-dump.txt.bz2").toURI());
        Assert.assertNotNull(dumpFile);
        Assert.assertTrue(Files.exists(dumpFile));

        dumpManager.parseDumpFile(dumpFile, false);
    }

    @Test
    public void testProcessLatestDumpFileOldEnough() throws URISyntaxException, IOException, ReplacerException {
        Path dumpFile = Paths.get(getClass().getResource("/es/bvalero/replacer/dump/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        Mockito.when(dumpFileFinder.findLatestDumpFile()).thenReturn(dumpFile);

        // Make the dump file old enough
        dumpManager.setDumpIndexWait(1); // 1 day
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        FileTime fileTime = FileTime.from(twoDaysAgo.toInstant(ZoneOffset.UTC));
        Files.setLastModifiedTime(dumpFile, fileTime);

        Mockito.when(dumpHandler.getProcessStatus()).thenReturn(new DumpIndexation());

        dumpManager.processLatestDumpFile(false);

        Mockito.verify(dumpHandler, Mockito.times(1)).startDocument();
    }

    @Test
    public void testProcessLatestDumpFileTooRecent() throws URISyntaxException, IOException, ReplacerException {
        Path dumpFile = Paths.get(getClass().getResource("/es/bvalero/replacer/dump/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        Mockito.when(dumpFileFinder.findLatestDumpFile()).thenReturn(dumpFile);

        // Make the dump file too recent
        dumpManager.setDumpIndexWait(1); // 1 day
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusHours(1);
        FileTime fileTime = FileTime.from(twoDaysAgo.toInstant(ZoneOffset.UTC));
        Files.setLastModifiedTime(dumpFile, fileTime);

        Mockito.when(dumpHandler.getProcessStatus()).thenReturn(new DumpIndexation());

        dumpManager.processLatestDumpFile(false);

        Mockito.verify(dumpHandler, Mockito.times(0)).startDocument();
    }

    @Test
    public void testProcessLatestDumpFileTooRecentForced() throws URISyntaxException, IOException, ReplacerException {
        Path dumpFile = Paths.get(getClass().getResource("/es/bvalero/replacer/dump/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        Mockito.when(dumpFileFinder.findLatestDumpFile()).thenReturn(dumpFile);

        // Make the dump file too recent
        dumpManager.setDumpIndexWait(1); // 1 day
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusHours(1);
        FileTime fileTime = FileTime.from(twoDaysAgo.toInstant(ZoneOffset.UTC));
        Files.setLastModifiedTime(dumpFile, fileTime);

        Mockito.when(dumpHandler.getProcessStatus()).thenReturn(new DumpIndexation());

        dumpManager.processLatestDumpFile(true);

        Mockito.verify(dumpHandler, Mockito.times(1)).startDocument();
    }

    @Test
    public void testProcessLatestDumpFileWithException() throws ReplacerException {
        Mockito.when(dumpFileFinder.findLatestDumpFile()).thenThrow(ReplacerException.class);
        Mockito.when(dumpHandler.getProcessStatus()).thenReturn(new DumpIndexation());

        dumpManager.processLatestDumpFile(false);

        Mockito.verify(dumpHandler, Mockito.times(0)).startDocument();
    }

    @Test
    public void testProcessLatestDumpFileAlreadyRunning() {
        DumpIndexation status = new DumpIndexation();
        status.setRunning(true);
        Mockito.when(dumpHandler.getProcessStatus()).thenReturn(status);

        dumpManager.processLatestDumpFile(false);

        Mockito.verify(dumpHandler, Mockito.times(0)).startDocument();
    }

    @Test
    public void testProcessLatestDumpFileAlreadyProcessed() throws URISyntaxException, ReplacerException {
        Path dumpFile = Paths.get(getClass().getResource("/es/bvalero/replacer/dump/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        Mockito.when(dumpFileFinder.findLatestDumpFile()).thenReturn(dumpFile);
        
        DumpIndexation status = new DumpIndexation("eswiki-20170101-pages-articles.xml.bz2", false);
        Mockito.when(dumpHandler.getProcessStatus()).thenReturn(status);

        dumpManager.processLatestDumpFile(false);

        Mockito.verify(dumpHandler, Mockito.times(0)).startDocument();
    }

    @Test
    public void testProcessLatestDumpFileAlreadyProcessedForced() throws URISyntaxException, ReplacerException {
        Path dumpFile = Paths.get(getClass().getResource("/es/bvalero/replacer/dump/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        Mockito.when(dumpFileFinder.findLatestDumpFile()).thenReturn(dumpFile);
        
        Mockito.when(dumpHandler.getProcessStatus()).thenReturn(new DumpIndexation());

        dumpManager.processLatestDumpFile(true);

        Mockito.verify(dumpHandler, Mockito.times(1)).startDocument();
    }

    @Test
    public void testProcessDumpScheduled() throws URISyntaxException, IOException, ReplacerException {
        Path dumpFile = Paths.get(getClass().getResource("/es/bvalero/replacer/dump/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        Mockito.when(dumpFileFinder.findLatestDumpFile()).thenReturn(dumpFile);
        
        // Make the dump file old enough
        Files.setLastModifiedTime(dumpFile, FileTime.from(LocalDateTime.now().minusDays(2).toInstant(ZoneOffset.UTC)));

        Mockito.when(dumpHandler.getProcessStatus()).thenReturn(new DumpIndexation());

        dumpManager.processDumpScheduled();

        Mockito.verify(dumpHandler, Mockito.times(1)).startDocument();
    }

    @Test
    public void testGetDumpStatus() {
        dumpManager.getDumpStatus();
        Mockito.verify(dumpHandler, Mockito.times(1)).getProcessStatus();
    }

}
