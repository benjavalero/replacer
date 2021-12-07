package es.bvalero.replacer.repository;

import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
public class TypeSubtypeCount {

    @NonNull
    String type;

    @NonNull
    String subtype;

    @NonNull
    Long count;
}
