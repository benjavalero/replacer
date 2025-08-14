package es.bvalero.replacer.common;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.StandardType;
import lombok.Value;

@Value(staticConstructor = "of")
public class PageCountRemoveEvent {

    WikipediaLanguage lang;
    StandardType type;
}
