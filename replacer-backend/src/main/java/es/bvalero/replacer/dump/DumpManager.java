package es.bvalero.replacer.dump;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service to manage the dump indexing tasks,
 * in particular to get the status of the current (or latest) indexing
 * and start a new one.
 *
 * The indexing will be executed periodically in @{@link DumpScheduledTask},
 * or manually from @{@link DumpController}.
 * The dumps are parsed with @{@link DumpParser}.
 */
@Slf4j
@Service
class DumpManager {

    @Autowired
    private DumpFinder dumpFinder;

    @Autowired
    private DumpParser dumpParser;

    /**
     * Find the latest dump files for each language and index them.
     * In order to be asynchronous it must be public and called externally:
     * https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/annotation/EnableAsync.html
     */
    @Loggable(value = LogLevel.DEBUG, skipArgs = true, skipResult = true)
    @Async
    public void indexLatestDumpFiles() {
        // Check just in case the handler is already running
        if (isDumpIndexingRunning()) {
            LOGGER.warn("Dump indexing is already running");
            return;
        }

        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            dumpFinder
                .findLatestDumpFile(lang)
                .ifPresent(dumpFile -> {
                    try {
                        dumpParser.parseDumpFile(lang, dumpFile);
                    } catch (ReplacerException e) {
                        LOGGER.error("Error indexing latest dump file for lang {}", lang, e);
                    }
                });
        }
    }

    DumpIndexingStatus getDumpIndexingStatus() {
        return dumpParser.getDumpIndexingStatus();
    }

    private boolean isDumpIndexingRunning() {
        return this.getDumpIndexingStatus().getRunning();
    }
}
