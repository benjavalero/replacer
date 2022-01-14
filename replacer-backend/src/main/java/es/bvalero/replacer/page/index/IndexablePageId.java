package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import lombok.Value;
import org.springframework.lang.NonNull;

@Value(staticConstructor = "of")
class IndexablePageId {

    @NonNull
    WikipediaLanguage lang;

    int pageId;
}
