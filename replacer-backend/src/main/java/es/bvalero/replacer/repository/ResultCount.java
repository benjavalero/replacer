package es.bvalero.replacer.repository;

import lombok.Value;
import org.springframework.lang.NonNull;

@Value(staticConstructor = "of")
public class ResultCount<T> implements Comparable<ResultCount<T>> {

    @NonNull
    T key;

    int count;

    @Override
    public int compareTo(ResultCount count) {
        return Integer.compare(count.getCount(), this.count);
    }
}
