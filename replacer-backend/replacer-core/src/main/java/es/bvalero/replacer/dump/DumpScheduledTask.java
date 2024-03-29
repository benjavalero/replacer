package es.bvalero.replacer.dump;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Task to index the latest dumps periodically */
@Slf4j
@Component
class DumpScheduledTask {

    // Dependency injection
    private final DumpIndexService dumpIndexService;

    DumpScheduledTask(DumpIndexService dumpIndexService) {
        this.dumpIndexService = dumpIndexService;
    }

    @Scheduled(
        initialDelayString = "${replacer.dump.batch.delay.initial}",
        fixedDelayString = "${replacer.dump.batch.delay}"
    )
    void scheduledStartDumpIndexing() {
        LOGGER.debug("START Scheduled Dump Indexing...");
        dumpIndexService.indexLatestDumpFiles();
    }
}
