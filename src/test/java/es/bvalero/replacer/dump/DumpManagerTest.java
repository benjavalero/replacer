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
    private DumpHandler dumpHandler;

    @InjectMocks
    private DumpManager dumpManager;

    @Before
    public void setUp() {
        dumpManager = new DumpManager();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRunIndexationWithBz2Dump() throws FileNotFoundException {
        Assert.assertNull(dumpManager.getStatus().getStartDate());
        Assert.assertNull(dumpManager.getStatus().getEndDate());

        int numArticles = 13;
        String bz2Path = getClass().getResource("/pages-articles.xml.bz2").getFile();
        DumpFile dumpFile = new DumpFile();
        dumpFile.setFile(new File(bz2Path));
        Mockito.when(dumpFinder.findLatestDumpFile(Mockito.any(File.class))).thenReturn(dumpFile);
        Mockito.when(dumpHandler.getNumProcessedItems()).thenReturn(numArticles);
        dumpManager.setDumpFolderPath("");

        dumpManager.runIndexation();

        Mockito.verify(dumpFinder, Mockito.times(1)).findLatestDumpFile(Mockito.any(File.class));
        Mockito.verify(dumpHandler, Mockito.times(1)).startDocument();

        Assert.assertNotNull(dumpManager.getStatus().getStartDate());
        Assert.assertNotNull(dumpManager.getStatus().getEndDate());
        Assert.assertFalse(dumpManager.getStatus().getStartDate().after(dumpManager.getStatus().getEndDate()));
        Assert.assertEquals(numArticles, dumpManager.getStatus().getNumProcessedItems());
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
        GregorianCalendar today = new GregorianCalendar();
        GregorianCalendar yesterday = new GregorianCalendar();
        yesterday.add(GregorianCalendar.DATE, -1);

        DumpFile dumpFile = new DumpFile();
        dumpFile.setDate(yesterday.getTime());
        Mockito.when(dumpFinder.findLatestDumpFile(Mockito.any(File.class))).thenReturn(dumpFile);

        dumpManager.getStatus().setEndDate(today.getTime());
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
