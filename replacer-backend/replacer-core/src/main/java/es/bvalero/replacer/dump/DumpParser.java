package es.bvalero.replacer.dump;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import java.util.Optional;

/** Service to read a Wikipedia dump, extract the pages and index them. */
interface DumpParser {
    void parseDumpFile(WikipediaLanguage lang, DumpFile dumpFile) throws ReplacerException;

    Optional<DumpStatus> getDumpStatus();
}
