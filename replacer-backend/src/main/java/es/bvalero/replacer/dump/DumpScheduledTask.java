package es.bvalero.replacer.dump;

import com.jcabi.aspects.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Task to index the latest dumps periodically */
@Component
public class DumpScheduledTask {

    @Autowired
    private DumpManager dumpManager;

    @Loggable(prepend = true)
    @Scheduled(
        initialDelayString = "${replacer.dump.batch.delay.initial}",
        fixedDelayString = "${replacer.dump.batch.delay}"
    )
    public void scheduledStartDumpIndexing() {
        dumpManager.indexLatestDumpFiles();
    }
}
