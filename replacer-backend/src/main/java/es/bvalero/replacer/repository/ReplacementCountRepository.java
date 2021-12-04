package es.bvalero.replacer.repository;

import es.bvalero.replacer.common.domain.WikipediaLanguage;

public interface ReplacementCountRepository {
    void reviewAsSystemByType(WikipediaLanguage lang, String type, String subtype);
}
