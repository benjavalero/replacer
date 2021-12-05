package es.bvalero.replacer.replacement.count;

import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
class TypeSubtypeCount {

    @NonNull
    String type;

    @NonNull
    String subtype;

    @NonNull
    Long count;
}
