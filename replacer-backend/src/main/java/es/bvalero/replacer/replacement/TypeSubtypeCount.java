package es.bvalero.replacer.replacement;

import lombok.Value;

@Value
class TypeSubtypeCount {

    String lang;
    String type;
    String subtype;
    long count;
}
