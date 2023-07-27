package es.bvalero.replacer.dump;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Task to index the latest dumps periodically */
@Slf4j
@Component
public class DumpScheduledTask {

    @Autowired
    private DumpManager dumpManager;

    @Scheduled(
        initialDelayString = "${replacer.dump.batch.delay.initial}",
        fixedDelayString = "${replacer.dump.batch.delay}"
    )
    public void scheduledStartDumpIndexing() {
        LOGGER.debug("START Scheduled Dump Indexing...");
        dumpManager.indexLatestDumpFiles();
    }
}
