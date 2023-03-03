package es.bvalero.replacer.finder;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import lombok.Value;

@Value(staticConstructor = "of")
public class ObsoleteReplacementType {

    WikipediaLanguage lang;
    StandardType type;
}
