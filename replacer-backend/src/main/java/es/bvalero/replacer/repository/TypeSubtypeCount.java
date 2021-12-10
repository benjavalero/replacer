package es.bvalero.replacer.repository;

import lombok.Value;
import org.springframework.lang.NonNull;

@Value(staticConstructor = "of")
public class TypeSubtypeCount {

    @NonNull
    String type;

    @NonNull
    String subtype;

    @NonNull
    Long count;
}
