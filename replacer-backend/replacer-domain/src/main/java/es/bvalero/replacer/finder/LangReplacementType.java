package es.bvalero.replacer.finder;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import lombok.Value;

@Value(staticConstructor = "of")
public class LangReplacementType {

    WikipediaLanguage lang;
    StandardType type;
}
