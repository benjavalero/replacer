package es.bvalero.replacer.finder.common;

import org.apache.commons.lang3.Range;

public interface FinderResult extends Comparable<FinderResult> {
    int getStart();
    String getText();

    default int getEnd() {
        return this.getStart() + this.getText().length();
    }

    default int compareTo(FinderResult o) {
        // Order descendant by start. If equals, the lower end.
        return o.getStart() == getStart() ? getEnd() - o.getEnd() : o.getStart() - getStart();
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
    default boolean contains(FinderResult r) {
        return this.getRange().containsRange(r.getRange());
    }
}
