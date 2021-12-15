package es.bvalero.replacer.repository;

import lombok.Value;
import org.springframework.lang.NonNull;

@Value(staticConstructor = "of")
public class ResultCount<T> {

    @NonNull
    T key;

    @NonNull
    Long count;
}
