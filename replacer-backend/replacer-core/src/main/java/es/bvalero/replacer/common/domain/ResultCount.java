package es.bvalero.replacer.common.domain;

import lombok.Value;
import org.springframework.lang.NonNull;

/** Generic class to store the result of different count queries */
@Value(staticConstructor = "of")
public class ResultCount<T> implements Comparable<ResultCount<T>> {

    @NonNull
    T key;

    int count;

    @Override
    public int compareTo(ResultCount count) {
        // By default, we sort the items by count in descendent order.
        return Integer.compare(count.getCount(), this.count);
    }
}
