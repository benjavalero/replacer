package es.bvalero.replacer.dump;

import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.domain.ReplacerException;
import java.nio.file.Path;

interface DumpJob {
    void parseDumpFile(Path dumpFile, WikipediaLanguage lang) throws ReplacerException;

    DumpIndexingStatus getDumpIndexingStatus();

    boolean isRunning();
}
