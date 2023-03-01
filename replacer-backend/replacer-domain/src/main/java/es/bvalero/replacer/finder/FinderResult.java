package es.bvalero.replacer.finder;

import java.util.Objects;
import org.apache.commons.lang3.Range;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.annotations.VisibleForTesting;

/** Base interface for the finder results: cosmetics, immutables and replacements. */
@VisibleForTesting
public interface FinderResult extends Comparable<FinderResult> {
    int getStart();
    String getText();

    default int getEnd() {
        return getStart() + getText().length();
    }

    default int compareTo(FinderResult o) {
        // Compare by start and then by end
        return Objects.equals(getStart(), o.getStart())
            ? Integer.compare(getEnd(), o.getEnd())
            : Integer.compare(getStart(), o.getStart());
    }

    private Range<Integer> getRange() {
        return Range.between(getStart(), getEnd() - 1);
    }

    default boolean intersects(FinderResult r) {
        return getRange().isOverlappedBy(r.getRange());
    }

    /** @return if a result contains strictly, i.e. not been equal, another result. */
    default boolean containsStrictly(FinderResult r) {
        // We don't want an item to contain itself
        return getRange().containsRange(r.getRange()) && !getRange().equals(r.getRange());
    }

    /** @return if a result contains (not strictly, i.e. both can be equal) another result. */
    default boolean contains(FinderResult r) {
        return getRange().containsRange(r.getRange());
    }

    @TestOnly
    default boolean validate(String pageContent) {
        // Validate positions only on tests not to penalize the performance
        if (getText().equals(pageContent.substring(getStart(), getEnd()))) {
            return true;
        } else {
            throw new IllegalArgumentException("Wrong positions in Finder Result");
        }
    }
}
