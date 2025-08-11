package es.bvalero.replacer.replacement.type;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.StandardType;

public interface ReplacementTypeSaveApi {
    /** Force index of a type to add the results to the database */
    void index(WikipediaLanguage lang, StandardType type);
}
