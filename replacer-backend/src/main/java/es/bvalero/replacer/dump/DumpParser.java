package es.bvalero.replacer.dump;

import es.bvalero.replacer.domain.ReplacerException;
import es.bvalero.replacer.domain.WikipediaLanguage;
import java.nio.file.Path;

interface DumpParser {
    void parseDumpFile(WikipediaLanguage lang, Path dumpFile) throws ReplacerException;

    DumpIndexingStatus getDumpIndexingStatus();

    boolean isDumpIndexingRunning();
}
