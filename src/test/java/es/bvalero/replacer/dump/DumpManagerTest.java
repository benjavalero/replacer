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
    public void testRunWithBz2Dump() throws FileNotFoundException {
        Assert.assertNull(dumpManager.getStartDate());
        Assert.assertNull(dumpManager.getEndDate());

        String bz2Path = getClass().getResource("/pages-articles.xml.bz2").getFile();
        Mockito.when(dumpFinder.findLatestDumpFile(Mockito.any(File.class)))
                .thenReturn(new File(bz2Path));

        dumpManager.setDumpFolderPath("");
        dumpManager.runIndexation();
        Mockito.verify(dumpFinder, Mockito.times(1))
                .findLatestDumpDate(Mockito.any(File.class));
        Mockito.verify(dumpFinder, Mockito.times(1))
                .findLatestDumpFile(Mockito.any(File.class));

        Assert.assertNotNull(dumpManager.getStartDate());
        Assert.assertNotNull(dumpManager.getEndDate());
        Assert.assertFalse(dumpManager.getStartDate().after(dumpManager.getEndDate()));
    }

    @Test
    public void testRunWithLastExecutionAfterDumpDate() throws FileNotFoundException {
        GregorianCalendar today = new GregorianCalendar();
        GregorianCalendar yesterday = new GregorianCalendar();
        yesterday.add(GregorianCalendar.DATE, -1);

        dumpManager.setEndDate(today.getTime());
        Mockito.when(dumpFinder.findLatestDumpDate(Mockito.any(File.class)))
                .thenReturn(yesterday.getTime());

        dumpManager.setDumpFolderPath("");
        dumpManager.runIndexation();
        Mockito.verify(dumpFinder, Mockito.times(1))
                .findLatestDumpDate(Mockito.any(File.class));
        Mockito.verify(dumpFinder, Mockito.times(0))
                .findLatestDumpFile(Mockito.any(File.class));
    }

}
