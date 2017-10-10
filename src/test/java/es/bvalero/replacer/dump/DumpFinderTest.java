package es.bvalero.replacer.dump;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.GregorianCalendar;

public class DumpFinderTest {

    @Test
    public void testFindLatestDumpFile() throws FileNotFoundException {
        File dumpFolder = Mockito.mock(File.class);
        File folder1 = new File("20170201");
        File folder2 = new File("20170101");
        File[] files = {folder1, folder2};
        Mockito.when(dumpFolder.listFiles(Mockito.any(FilenameFilter.class))).thenReturn(files);

        DumpFinder dumpFinder = new DumpFinder();
        Assert.assertEquals("20170201\\eswiki-20170201-pages-articles.xml.bz2",
                dumpFinder.findLatestDumpFile(dumpFolder).getPath());
    }

    @Test(expected = FileNotFoundException.class)
    public void testFindLatestDumpFileException() throws FileNotFoundException {
        File dumpFolder = Mockito.mock(File.class);
        File[] files = new File[0];
        Mockito.when(dumpFolder.listFiles(Mockito.any(FilenameFilter.class))).thenReturn(files);

        DumpFinder dumpFinder = new DumpFinder();
        dumpFinder.findLatestDumpFile(dumpFolder);
    }

    @Test
    public void testFindLatestDumpDate() throws FileNotFoundException {
        File dumpFolder = Mockito.mock(File.class);
        File folder1 = new File("20170201");
        File folder2 = new File("20170101");
        File[] files = {folder1, folder2};
        Mockito.when(dumpFolder.listFiles(Mockito.any(FilenameFilter.class))).thenReturn(files);

        DumpFinder dumpFinder = new DumpFinder();

        GregorianCalendar cal = new GregorianCalendar(2017, 1, 1);
        Assert.assertEquals(cal.getTime(), dumpFinder.findLatestDumpDate(dumpFolder));
    }

    @Test(expected = FileNotFoundException.class)
    public void testFindLatestDumpDateException() throws FileNotFoundException {
        File dumpFolder = Mockito.mock(File.class);
        File folder1 = new File("xxx");
        File[] files = {folder1};
        Mockito.when(dumpFolder.listFiles(Mockito.any(FilenameFilter.class))).thenReturn(files);

        DumpFinder dumpFinder = new DumpFinder();
        dumpFinder.findLatestDumpDate(dumpFolder);
    }

}
