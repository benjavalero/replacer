package es.bvalero.replacer.replacement.stats;

import es.bvalero.replacer.repository.ResultCount;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
class ReviewerCountMapper {

    Collection<ReviewerCount> fromModel(Collection<ResultCount<String>> counts) {
        return counts
            .stream()
            .sorted()
            .map(count -> ReviewerCount.of(count.getKey(), count.getCount()))
            .collect(Collectors.toUnmodifiableList());
    }
}
