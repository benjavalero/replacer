package es.bvalero.replacer.dump;

import es.bvalero.replacer.domain.ReplacerException;
import es.bvalero.replacer.domain.WikipediaLanguage;
import java.nio.file.Path;

/** Service to read a Wikipedia dump, extract the pages and process them. */
interface DumpParser {
    void parseDumpFile(WikipediaLanguage lang, Path dumpFile) throws ReplacerException;

    DumpIndexingStatus getDumpIndexingStatus();
}
