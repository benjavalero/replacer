package es.bvalero.replacer.repository.jdbc;

import es.bvalero.replacer.common.domain.WikipediaPageId;
import lombok.Value;
import org.springframework.lang.NonNull;

@Value(staticConstructor = "of")
class PageId {

    @NonNull
    String lang;

    @NonNull
    Integer pageId;

    static PageId of(WikipediaPageId wikipediaPageId) {
        return of(wikipediaPageId.getLang().getCode(), wikipediaPageId.getPageId());
    }
}
