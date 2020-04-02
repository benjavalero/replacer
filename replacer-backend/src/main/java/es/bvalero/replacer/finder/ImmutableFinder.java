package es.bvalero.replacer.finder;

import java.util.List;
import java.util.regex.MatchResult;
import org.apache.commons.collections4.IterableUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Interface to be implemented by any class returning a collection of immutables.
 *
 * For performance reasons, it is preferred to return them as an iterable.
 */
public interface ImmutableFinder extends Comparable<ImmutableFinder> {
    Iterable<Immutable> find(String text);

    default List<Immutable> findList(String text) {
        return IterableUtils.toList(find(text));
    }

    default Immutable convert(MatchResult match) {
        return Immutable.of(match.start(), match.group(), this.getClass().getSimpleName());
    }

    default ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.NONE;
    }

    default int compareTo(@NotNull ImmutableFinder finder) {
        return Integer.compare(finder.getPriority().getValue(), this.getPriority().getValue());
    }
}
