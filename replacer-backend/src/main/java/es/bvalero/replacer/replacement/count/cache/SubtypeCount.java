package es.bvalero.replacer.replacement.count.cache;

import lombok.Value;
import lombok.With;
import org.springframework.lang.NonNull;

@Value(staticConstructor = "of")
class SubtypeCount {

    @NonNull
    String subtype;

    @With
    @NonNull
    Long count;
}
