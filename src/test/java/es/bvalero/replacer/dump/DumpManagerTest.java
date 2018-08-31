package es.bvalero.replacer.dump;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.GregorianCalendar;

public class DumpManagerTest {

    @Mock
    private DumpFinder dumpFinder;

    @Mock
    private DumpProcessor dumpProcessor;

    @InjectMocks
    private DumpManager dumpManager;

    @Before
    public void setUp() {
        dumpManager = new DumpManager();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRunIndexationAlreadyRunning() throws FileNotFoundException {
        dumpManager.getDumpHandler().startDocument();

        dumpManager.runIndexation();

        Mockito.verify(dumpFinder, Mockito.times(0)).findLatestDumpFile(Mockito.any(File.class));
    }

    @Test
    public void testRunIndexationWithBz2Dump() throws FileNotFoundException {
        Assert.assertFalse(dumpManager.getStatus().isRunning());
        Assert.assertEquals(0.00, dumpManager.getStatus().getProgress(), 0.01);
        Assert.assertEquals(0, dumpManager.getStatus().getPagesCount());
        Assert.assertNull(dumpManager.getStatus().getLastRun());

        String bz2Path = getClass().getResource("/pages-articles.xml.bz2").getFile();
        DumpFile dumpFile = new DumpFile();
        dumpFile.setFile(new File(bz2Path));
        Mockito.when(dumpFinder.findLatestDumpFile(Mockito.any(File.class))).thenReturn(dumpFile);
        dumpManager.setDumpFolderPath("");

        dumpManager.runIndexation();

        Mockito.verify(dumpFinder, Mockito.times(1)).findLatestDumpFile(Mockito.any(File.class));
        Mockito.verify(dumpProcessor, Mockito.times(1)).finish();

        Assert.assertFalse(dumpManager.getStatus().isRunning());
        Assert.assertEquals(2, dumpManager.getStatus().getPagesCount());
        Assert.assertNotNull(dumpManager.getStatus().getAverage());
        Assert.assertNotNull(dumpManager.getStatus().getLastRun());
    }

    @Test
    public void testRunIndexationWithNonExistingDump() throws FileNotFoundException {
        Mockito.when(dumpFinder.findLatestDumpFile(Mockito.any(File.class)))
                .thenThrow(new FileNotFoundException());
        dumpManager.setDumpFolderPath("");

        dumpManager.runIndexation();

        Mockito.verify(dumpFinder, Mockito.times(1))
                .findLatestDumpFile(Mockito.any(File.class));
    }

    @Test
    public void testRunWithLastExecutionAfterDumpDate() throws FileNotFoundException {
        GregorianCalendar yesterday = new GregorianCalendar();
        yesterday.add(GregorianCalendar.DATE, -1);

        DumpFile dumpFile = new DumpFile();
        dumpFile.setDate(yesterday.getTime());
        Mockito.when(dumpFinder.findLatestDumpFile(Mockito.any(File.class))).thenReturn(dumpFile);

        dumpManager.getDumpHandler().endDocument();
        dumpManager.setDumpFolderPath("");

        dumpManager.runIndexation();

        Mockito.verify(dumpFinder, Mockito.times(1))
                .findLatestDumpFile(Mockito.any(File.class));
    }

    @Test
    public void testRunIndexationWithParseException() throws FileNotFoundException {
        String nonValidFilePath = getClass().getResource("/false-positives.txt").getFile();
        DumpFile dumpFile = new DumpFile();
        dumpFile.setFile(new File(nonValidFilePath));
        Mockito.when(dumpFinder.findLatestDumpFile(Mockito.any(File.class))).thenReturn(dumpFile);
        dumpManager.setDumpFolderPath(" ");

        dumpManager.runIndexation();

        Mockito.verify(dumpFinder, Mockito.times(1))
                .findLatestDumpFile(Mockito.any(File.class));
    }

}
