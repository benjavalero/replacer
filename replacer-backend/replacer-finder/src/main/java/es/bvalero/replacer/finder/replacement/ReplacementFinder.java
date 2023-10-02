package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.Replacement;
import java.util.Optional;

public interface ReplacementFinder extends Finder<Replacement> {
    @Override
    default FinderPriority getPriority() {
        return FinderPriority.MEDIUM;
    }

    default Optional<StandardType> findMatchingReplacementType(
        WikipediaLanguage lang,
        String replacement,
        boolean caseSensitive
    ) {
        return Optional.empty();
    }
}
