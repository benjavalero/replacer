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
import java.util.Date;
import java.util.GregorianCalendar;

public class DumpParserTest {

    @Mock
    private DumpFinder dumpFinder;

    @InjectMocks
    private DumpParser task;

    @Before
    public void setUp() {
        task = new DumpParser();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRunWithBz2Dump() throws FileNotFoundException {
        Assert.assertNull(task.getDtStart());
        Assert.assertNull(task.getDtEnd());

        String bz2Path = getClass().getResource("/pages-articles.xml.bz2").getFile();
        Mockito.when(dumpFinder.findLatestDumpFile()).thenReturn(new File(bz2Path));

        task.run();
        Assert.assertNotNull(task.getDtStart());
        Assert.assertNotNull(task.getDtEnd());
        Assert.assertFalse(task.getDtStart().after(task.getDtEnd()));
        Mockito.verify(dumpFinder, Mockito.times(1)).findLatestDumpDate();
        Mockito.verify(dumpFinder, Mockito.times(1)).findLatestDumpFile();
    }

    @Test
    public void testRunWithLastExecutionAfterDumpDate() throws FileNotFoundException {
        GregorianCalendar today = new GregorianCalendar();
        GregorianCalendar yesterday = new GregorianCalendar();
        yesterday.add(GregorianCalendar.DATE, -1);

        task.setDtEnd(today.getTime());
        Mockito.when(dumpFinder.findLatestDumpDate()).thenReturn(yesterday.getTime());

        task.run();
        Mockito.verify(dumpFinder, Mockito.times(1)).findLatestDumpDate();
        Mockito.verify(dumpFinder, Mockito.times(0)).findLatestDumpFile();
    }

    @Test
    public void testIsRunning() {
        Assert.assertFalse(task.isRunning());

        Date now = new Date();
        task.setDtStart(now);
        Assert.assertTrue(task.isRunning());

        task.setDtEnd(now);
        Assert.assertFalse(task.isRunning());
    }

}
