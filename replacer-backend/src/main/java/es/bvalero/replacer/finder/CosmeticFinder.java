package es.bvalero.replacer.finder;

import java.util.List;
import java.util.Optional;
import java.util.regex.MatchResult;
import org.apache.commons.collections4.IterableUtils;

/**
 * Interface to be implemented by any class returning a collection of cosmetics.
 */
public interface CosmeticFinder {
    Iterable<Cosmetic> find(String text);

    default List<Cosmetic> findList(String text) {
        return IterableUtils.toList(find(text));
    }

    default Cosmetic convert(MatchResult match) {
        return Cosmetic.of(match.start(), match.group(), getFix(match));
    }

    // To be always overwritten
    default String getFix(MatchResult match) {
        return match.group();
    }

    default Optional<Integer> getFixId() {
        return Optional.empty();
    }
}
