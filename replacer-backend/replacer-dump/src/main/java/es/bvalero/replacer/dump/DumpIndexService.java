package es.bvalero.replacer.dump;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service to manage the dump indexing tasks, in particular to get the status
 * of the current (or latest) indexing and start a new one.
 * The indexing will be executed periodically in @{@link DumpScheduledTask},
 * or manually from DumpController.
 * The dumps are parsed with @{@link DumpParser}.
 */
@Slf4j
@Service
class DumpIndexService implements DumpIndexApi {

    // Dependency injection
    private final DumpFinder dumpFinder;
    private final DumpParser dumpParser;

    DumpIndexService(DumpFinder dumpFinder, DumpParser dumpParser) {
        this.dumpFinder = dumpFinder;
        this.dumpParser = dumpParser;
    }

    // In order to be asynchronous it must be public and called externally:
    // https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/annotation/EnableAsync.html
    @Async
    @Override
    public void indexLatestDumpFiles() {
        LOGGER.debug("START dump indexing...");

        // Check just in case the handler is already running
        if (isDumpIndexingRunning()) {
            LOGGER.warn("Dump indexing is already running. Abort.");
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

        LOGGER.debug("END dump indexing...");
    }

    @Override
    public Optional<DumpStatus> getDumpStatus() {
        return dumpParser.getDumpStatus();
    }

    private boolean isDumpIndexingRunning() {
        return getDumpStatus().isPresent() && getDumpStatus().get().isRunning();
    }
}
