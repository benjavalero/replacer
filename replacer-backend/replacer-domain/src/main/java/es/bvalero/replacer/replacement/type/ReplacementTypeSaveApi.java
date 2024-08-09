package es.bvalero.replacer.replacement.type;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;

public interface ReplacementTypeSaveApi {
    /** Delete all the replacements to review by type */
    void remove(WikipediaLanguage lang, StandardType type);

    /** Force index of a type to add the results to the database */
    void index(WikipediaLanguage lang, StandardType type);
}
