package es.bvalero.replacer.dump;

import com.github.rozidan.springboot.logger.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Task to index the latest dumps periodically */
@Component
public class DumpScheduledTask {

    @Autowired
    private DumpManager dumpManager;

    @Loggable(value = LogLevel.DEBUG, entered = true, skipArgs = true, skipResult = true)
    @Scheduled(
        initialDelayString = "${replacer.dump.batch.delay.initial}",
        fixedDelayString = "${replacer.dump.batch.delay}"
    )
    public void scheduledStartDumpIndexing() {
        dumpManager.indexLatestDumpFiles();
    }
}
