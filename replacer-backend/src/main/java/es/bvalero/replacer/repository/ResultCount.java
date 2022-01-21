package es.bvalero.replacer.repository;

import es.bvalero.replacer.common.domain.Suggestion;
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

    public ResultCount<T> merge(ResultCount<T> resultCount) {
        if (!this.getKey().equals(resultCount.getKey())) {
            throw new IllegalArgumentException();
        }

        int mergedCount = this.getCount() + resultCount.getCount();
        return ResultCount.of(this.getKey(), mergedCount);
    }
}
