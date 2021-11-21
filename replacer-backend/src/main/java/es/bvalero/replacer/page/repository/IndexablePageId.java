package es.bvalero.replacer.page.repository;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import lombok.Value;
import org.springframework.lang.NonNull;

@Value(staticConstructor = "of")
public class IndexablePageId {

    @NonNull
    WikipediaLanguage lang;

    @NonNull
    Integer pageId;
}
