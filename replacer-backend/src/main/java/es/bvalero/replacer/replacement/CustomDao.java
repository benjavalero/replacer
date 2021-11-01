package es.bvalero.replacer.replacement;

import es.bvalero.replacer.domain.WikipediaLanguage;
import java.util.List;

interface CustomDao {
    void insert(CustomEntity entity);

    List<Integer> findPageIdsReviewed(WikipediaLanguage lang, String replacement, boolean cs);
}
