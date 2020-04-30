package es.bvalero.replacer.finder;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.List;
import java.util.regex.MatchResult;
import org.apache.commons.collections4.IterableUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

/**
 * Interface to be implemented by any class returning a collection of immutables.
 *
 * For performance reasons, it is preferred to return them as an iterable.
 */
public interface ImmutableFinder extends Comparable<ImmutableFinder> {
    Iterable<Immutable> find(String text, WikipediaLanguage lang);

    @TestOnly
    default List<Immutable> findList(String text) {
        return IterableUtils.toList(find(text, WikipediaLanguage.ALL));
    }

    default Immutable convert(MatchResult match) {
        return Immutable.of(match.start(), match.group(), this);
    }

    default ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.NONE;
    }

    default int compareTo(@NotNull ImmutableFinder finder) {
        return Integer.compare(finder.getPriority().getValue(), this.getPriority().getValue());
    }

    default int getMaxLength() {
        return Integer.MAX_VALUE;
    }
}
