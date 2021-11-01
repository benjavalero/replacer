package es.bvalero.replacer.dump;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.domain.ReplacerException;
import java.nio.file.Path;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Find the Wikipedia dumps in the filesystem where the application runs.
 * This indexing will be done periodically, or manually from @{@link DumpController}.
 * The dumps are parsed with @{@link DumpJob}.
 * Each page found in the dump is processed in @{@link DumpPageProcessor}.
 */
@Slf4j
@Component
class DumpManager {

    @Autowired
    private DumpFinder dumpFinder;

    @Setter // For testing
    @Autowired
    private DumpJob dumpJob;

    /**
     * Check if there is a new dump to process.
     */
    @Loggable(prepend = true)
    @Scheduled(
        initialDelayString = "${replacer.dump.batch.delay.initial}",
        fixedDelayString = "${replacer.dump.batch.delay}"
    )
    public void scheduledStartDumpIndexing() {
        processLatestDumpFiles();
    }

    /**
     * Find the latest dump file and process it
     */
    // In order to be asynchronous it must be public and called externally:
    // https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/annotation/EnableAsync.html
    @Loggable(value = Loggable.DEBUG)
    @Async
    public void processLatestDumpFiles() {
        // Check just in case the handler is already running
        if (dumpJob.isRunning()) {
            LOGGER.warn("Dump indexing is already running.");
            return;
        }

        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            try {
                Path latestDumpFileFound = dumpFinder.findLatestDumpFile(lang);
                dumpJob.parseDumpFile(latestDumpFileFound, lang);
            } catch (ReplacerException e) {
                LOGGER.error("Error indexing latest dump file for lang {}", lang, e);
            }
        }
    }

    DumpIndexingStatus getDumpIndexingStatus() {
        return dumpJob.getDumpIndexingStatus();
    }
}
