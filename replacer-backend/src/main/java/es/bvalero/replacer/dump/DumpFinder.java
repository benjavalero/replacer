package es.bvalero.replacer.dump;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import java.nio.file.Path;

/** Service to find the latest Wikipedia dump in order to parse and index it */
interface DumpFinder {
    Path findLatestDumpFile(WikipediaLanguage lang) throws ReplacerException;
}
