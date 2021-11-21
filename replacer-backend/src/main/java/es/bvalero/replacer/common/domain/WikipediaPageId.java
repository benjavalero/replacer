package es.bvalero.replacer.common.domain;

import lombok.Value;
import org.springframework.lang.NonNull;

@Value(staticConstructor = "of")
public class WikipediaPageId {

    @NonNull
    WikipediaLanguage lang;

    @NonNull
    Integer pageId;
}
