package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;

public interface RemoveObsoleteReplacementType {
    default void removeObsoleteReplacementTypes(WikipediaLanguage lang, Collection<ReplacementType> types) {
        // Do nothing
    }
}
