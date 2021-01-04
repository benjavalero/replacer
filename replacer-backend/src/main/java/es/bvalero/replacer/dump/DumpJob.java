package es.bvalero.replacer.dump;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.nio.file.Path;

interface DumpJob {
    void parseDumpFile(Path dumpFile, WikipediaLanguage lang) throws ReplacerException;

    DumpIndexingStatus getDumpIndexingStatus();

    boolean isRunning();
}
