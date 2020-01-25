package es.bvalero.replacer.finder2;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Interface to be implemented by any class returning a collection of immutables.
 *
 * For performance reasons, it is preferred to return them as an iterable.
 */
public interface ImmutableFinder {
    Iterable<Immutable> find(String text);

    default List<Immutable> findList(String text) {
        return StreamSupport.stream(find(text).spliterator(), false).collect(Collectors.toList());
    }

    default boolean isValid(MatchResult match, String text) {
        return true;
    }

    default Immutable convert(MatchResult match) {
        return Immutable.of(match.start(), match.group());
    }
}
