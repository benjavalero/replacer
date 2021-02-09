package es.bvalero.replacer.replacement;

import lombok.Value;

@Value(staticConstructor = "of")
class TypeSubtypeCount {

    String type;
    String subtype;
    long count;
}
