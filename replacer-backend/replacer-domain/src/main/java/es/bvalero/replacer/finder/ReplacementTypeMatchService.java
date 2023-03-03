package es.bvalero.replacer.finder;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Optional;

public interface ReplacementTypeMatchService {
    Optional<StandardType> findMatchingReplacementType(
        WikipediaLanguage lang,
        String replacement,
        boolean caseSensitive
    );
}
