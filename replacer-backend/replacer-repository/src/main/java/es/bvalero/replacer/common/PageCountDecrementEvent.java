package es.bvalero.replacer.common;

import es.bvalero.replacer.finder.LangReplacementType;
import lombok.Value;

@Value(staticConstructor = "of")
public class PageCountDecrementEvent {

    LangReplacementType replacementType;
}
