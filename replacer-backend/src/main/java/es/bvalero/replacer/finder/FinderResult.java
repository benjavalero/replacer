package es.bvalero.replacer.finder;

import java.util.Collection;
import java.util.Objects;
import org.apache.commons.lang3.Range;
import org.jetbrains.annotations.TestOnly;

public interface FinderResult extends Comparable<FinderResult> {
    int getStart();
    String getText();

    default int getEnd() {
        return this.getStart() + this.getText().length();
    }

    default int compareTo(FinderResult o) {
        // Compare by start and then by end
        return Objects.equals(this.getStart(), o.getStart())
            ? Integer.compare(this.getEnd(), o.getEnd())
            : Integer.compare(this.getStart(), o.getStart());
    }

    private Range<Integer> getRange() {
        return Range.between(this.getStart(), this.getEnd() - 1);
    }

    default boolean intersects(FinderResult r) {
        return this.getRange().isOverlappedBy(r.getRange());
    }

    /** @return if a result contains strictly, i.e. not been equal, another result. */
    default boolean containsStrictly(FinderResult r) {
        // We don't want an item to contain itself
        return this.getRange().containsRange(r.getRange()) && !this.getRange().equals(r.getRange());
    }

    /** @return if a result contains (not strictly, i.e. both can be equal) another result. */
    @TestOnly
    default boolean contains(FinderResult r) {
        return this.getRange().containsRange(r.getRange());
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

    static void removeNested(Collection<? extends FinderResult> results) {
        // Filter to return the results which are NOT strictly contained in any other
        results.removeIf(r -> results.stream().anyMatch(r2 -> r2.containsStrictly(r)));
    }
}
