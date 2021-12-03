package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Set;

public interface ReplacementDao {
    ///// MISSPELLING MANAGER

    void deleteToBeReviewedBySubtype(WikipediaLanguage lang, String type, Set<String> subtypes);
}
