package es.bvalero.replacer.repository;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;

public interface CustomRepository {
    /** Add a custom replacement */
    void addCustom(CustomModel entity);

    /** Find the pages reviewed for the given custom replacement and return the IDs */
    Collection<Integer> findPageIdsReviewed(WikipediaLanguage lang, String replacement, boolean cs);
}