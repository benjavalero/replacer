package es.bvalero.replacer.dump;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import es.bvalero.replacer.ReplacerException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

public class DumpManagerTest {

    @Mock
    public DumpFinder dumpFinder;

    @Mock
    private JobExplorer jobExplorer;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job dumpJob;

    @InjectMocks
    private DumpManager dumpManager;

    @Before
    public void setUp() {
        dumpManager = new DumpManager();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testParseDumpFile() throws URISyntaxException, ReplacerException, JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        // We need a real dump file to create the input stream
        Path dumpFile = Paths.get(getClass().getResource("/es/bvalero/replacer/dump/eswiki/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        Assert.assertNotNull(dumpFile);
        Assert.assertTrue(Files.exists(dumpFile));

        dumpManager.parseDumpFile(dumpFile);

        Mockito.verify(jobLauncher).run(Mockito.any(Job.class), Mockito.any(JobParameters.class));
    }

    @Test
    public void testProcessLatestDumpFileWithException() throws ReplacerException, JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        Mockito.when(dumpFinder.findLatestDumpFile()).thenThrow(ReplacerException.class);

        dumpManager.processLatestDumpFile();

        Mockito.verify(jobLauncher, Mockito.never()).run(Mockito.any(Job.class), Mockito.any(JobParameters.class));
    }

    @Test
    public void testProcessLatestDumpFileAlreadyRunning() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        Mockito.when(
        jobExplorer.findRunningJobExecutions(Mockito.anyString()))
            .thenReturn(Collections.singleton(Mockito.mock(JobExecution.class)));

        dumpManager.processLatestDumpFile();

        Mockito.verify(jobLauncher, Mockito.never()).run(Mockito.any(Job.class), Mockito.any(JobParameters.class));
    }

    @Test
    public void testProcessDumpScheduled() throws URISyntaxException, IOException, ReplacerException, JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        Path dumpFile = Paths.get(getClass().getResource("/es/bvalero/replacer/dump/eswiki/20170101/eswiki-20170101-pages-articles.xml.bz2").toURI());
        Mockito.when(dumpFinder.findLatestDumpFile()).thenReturn(dumpFile);

        // Make the dump file old enough
        Files.setLastModifiedTime(dumpFile, FileTime.from(LocalDateTime.now().minusDays(2).toInstant(ZoneOffset.UTC)));

        dumpManager.processDumpScheduled();

        Mockito.verify(jobLauncher, Mockito.times(1)).run(Mockito.any(Job.class), Mockito.any(JobParameters.class));
    }
}
