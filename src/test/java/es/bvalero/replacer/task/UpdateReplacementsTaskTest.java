package es.bvalero.replacer.task;

import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FilenameFilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;

public class UpdateReplacementsTaskTest {

    @Test
    public void testFindLatestDumpPath() {
        File dumpFolderFile = mock(File.class);
        File folder1 = new File("20170201");
        File folder2 = new File("20170101");
        File[] files = {folder1, folder2};
        Mockito.when(dumpFolderFile.listFiles(any(FilenameFilter.class))).thenReturn(files);

        UpdateReplacementsTask task = new UpdateReplacementsTask();
        String dumpPath = task.findLatestDumpPath(dumpFolderFile);
        assertNotNull(dumpPath);
        assertEquals("20170201/eswiki-20170201-pages-articles.xml.bz2", dumpPath);
    }

}
