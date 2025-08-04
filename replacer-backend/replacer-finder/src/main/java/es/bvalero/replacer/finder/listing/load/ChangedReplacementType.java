package es.bvalero.replacer.finder.listing.load;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.StandardType;
import lombok.Value;

@Value(staticConstructor = "of")
class ChangedReplacementType {

    WikipediaLanguage lang;
    StandardType type;
}
