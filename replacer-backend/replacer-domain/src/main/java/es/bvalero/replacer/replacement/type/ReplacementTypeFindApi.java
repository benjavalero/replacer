package es.bvalero.replacer.replacement.type;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.StandardType;
import java.util.Optional;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

@PrimaryPort
interface ReplacementTypeFindApi {
    /** Find a known standard type matching with the given replacement and case-sensitive option */
    Optional<StandardType> findReplacementType(WikipediaLanguage lang, String replacement, boolean caseSensitive);
}
