package es.bvalero.replacer.replacement;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import lombok.Value;

@Value
class TypeSubtypeCount {

    WikipediaLanguage lang;
    String type;
    String subtype;
    long count;
}
