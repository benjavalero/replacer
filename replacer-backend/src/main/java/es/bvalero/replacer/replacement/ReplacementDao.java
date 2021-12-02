package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.List;
import java.util.Set;

public interface ReplacementDao {
    ///// PAGE LISTS

    List<String> findPageTitlesToReviewBySubtype(WikipediaLanguage lang, String type, String subtype);

    ///// MISSPELLING MANAGER

    void deleteToBeReviewedBySubtype(WikipediaLanguage lang, String type, Set<String> subtypes);
}
