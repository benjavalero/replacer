package es.bvalero.replacer.dump;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import java.nio.file.Path;

/** Service to read a Wikipedia dump, extract the pages and index them. */
interface DumpParser {
    void parseDumpFile(WikipediaLanguage lang, Path dumpFile) throws ReplacerException;

    DumpIndexingStatus getDumpIndexingStatus();
}
