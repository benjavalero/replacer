package es.bvalero.replacer.page;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.StandardType;
import lombok.Value;

@Value(staticConstructor = "of")
public class PageCountDecrementEvent {

    WikipediaLanguage lang;
    StandardType type;
}
