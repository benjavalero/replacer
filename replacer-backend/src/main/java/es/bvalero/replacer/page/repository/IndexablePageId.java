package es.bvalero.replacer.page.repository;

import es.bvalero.replacer.domain.WikipediaLanguage;
import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
public class IndexablePageId {

    @NonNull
    WikipediaLanguage lang;

    @NonNull
    Integer pageId;
}
