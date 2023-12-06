package es.bvalero.replacer.finder;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Optional;

// TODO: This interface should be annotated with @SecondaryPort but it is currently called directly by a controller
public interface ReplacementTypeFindService {
    /** Find a known standard type matching with the given replacement and case-sensitive option */
    Optional<StandardType> findReplacementType(WikipediaLanguage lang, String replacement, boolean caseSensitive);
}
