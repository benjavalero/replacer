package es.bvalero.replacer.repository.jdbc;

import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.repository.ReplacementRepository;
import lombok.Value;
import org.springframework.lang.NonNull;

@Value(staticConstructor = "of")
class PageId {

    @NonNull
    String lang;

    @NonNull
    Integer pageId;

    // For the named-parameter mapping
    @SuppressWarnings("unused")
    String getSystem() {
        return ReplacementRepository.REVIEWER_SYSTEM;
    }

    static PageId of(WikipediaPageId wikipediaPageId) {
        return of(wikipediaPageId.getLang().getCode(), wikipediaPageId.getPageId());
    }
}
