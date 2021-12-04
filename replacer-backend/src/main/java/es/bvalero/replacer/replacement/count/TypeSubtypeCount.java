package es.bvalero.replacer.replacement.count;

import lombok.Value;

@Value(staticConstructor = "of")
class TypeSubtypeCount {

    String type;
    String subtype;
    long count;
}
