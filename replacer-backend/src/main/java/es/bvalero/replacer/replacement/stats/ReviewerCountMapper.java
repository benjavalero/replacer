package es.bvalero.replacer.replacement.stats;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
class ReviewerCountMapper {

    Collection<ReviewerCount> fromModel(Map<String, Long> countMap) {
        return countMap
            .entrySet()
            .stream()
            .map(entry -> ReviewerCount.of(entry.getKey(), entry.getValue()))
            .sorted()
            .collect(Collectors.toUnmodifiableList());
    }
}
