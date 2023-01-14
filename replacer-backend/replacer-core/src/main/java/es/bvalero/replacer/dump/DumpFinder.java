package es.bvalero.replacer.dump;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Optional;

/** Service to find the latest Wikipedia dump in order to index it */
interface DumpFinder {
    Optional<DumpFile> findLatestDumpFile(WikipediaLanguage lang);
}
