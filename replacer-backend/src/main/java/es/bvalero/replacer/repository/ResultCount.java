package es.bvalero.replacer.repository;

import lombok.Value;
import org.springframework.lang.NonNull;

@Value(staticConstructor = "of")
public class ResultCount<T> implements Comparable<ResultCount<T>> {

    @NonNull
    T key;

    @NonNull
    Long count;

    @Override
    public int compareTo(ResultCount count) {
        return count.getCount().compareTo(this.count);
    }
}
