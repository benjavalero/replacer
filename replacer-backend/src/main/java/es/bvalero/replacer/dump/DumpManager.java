package es.bvalero.replacer.dump;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.domain.ReplacerException;
import es.bvalero.replacer.domain.WikipediaLanguage;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Service to find the latest Wikipedia dumps, parse and index the found pages.
 *
 * The indexing will be executed periodically in @{@link DumpScheduledTask},
 * or manually from @{@link DumpController}.
 * The dumps are parsed with @{@link DumpParser}.
 * Each page found in the dump is processed in @{@link DumpPageProcessor}.
 */
@Slf4j
@Component
class DumpManager {

    @Autowired
    private DumpFinder dumpFinder;

    @Autowired
    private DumpParser dumpParser;

    /**
     * Find the latest dump files for each language and process them.
     * In order to be asynchronous it must be public and called externally:
     * https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/annotation/EnableAsync.html
     */
    @Loggable(value = Loggable.DEBUG)
    @Async
    public void processLatestDumpFiles() {
        // Check just in case the handler is already running
        if (dumpParser.isDumpIndexingRunning()) {
            LOGGER.warn("Dump indexing is already running.");
            return;
        }

        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            try {
                Path latestDumpFile = dumpFinder.findLatestDumpFile(lang);
                dumpParser.parseDumpFile(lang, latestDumpFile);
            } catch (ReplacerException e) {
                LOGGER.error("Error indexing latest dump file for lang {}", lang, e);
            }
        }
    }

    DumpIndexingStatus getDumpIndexingStatus() {
        return dumpParser.getDumpIndexingStatus();
    }
}
