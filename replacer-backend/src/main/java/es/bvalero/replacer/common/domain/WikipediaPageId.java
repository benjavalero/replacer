package es.bvalero.replacer.common.domain;

import lombok.Value;
import org.springframework.lang.NonNull;

/** Wikipedia page unique identifier */
@Value(staticConstructor = "of")
public class WikipediaPageId {

    @NonNull
    WikipediaLanguage lang;

    int pageId;

    @Override
    public String toString() {
        return String.format("%s - %d", lang, pageId);
    }
}
