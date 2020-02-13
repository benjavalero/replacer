package es.bvalero.replacer.finder;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.collections4.IterableUtils;

/**
 * Interface to be implemented by any class returning a collection of replacements.
 */
public interface ReplacementFinder {
    Iterable<Replacement> find(String text);

    default Stream<Replacement> findStream(String text) {
        return StreamSupport.stream(find(text).spliterator(), false);
    }

    default List<Replacement> findList(String text) {
        return IterableUtils.toList(find(text));
    }

    default boolean isValidMatch(MatchResult match, String text) {
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), text);
    }
}
