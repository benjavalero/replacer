package es.bvalero.replacer.finder.common;

import com.google.common.collect.Range;

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
        return Range.closedOpen(this.getStart(), this.getEnd());
    }

    default boolean intersects(FinderResult r) {
        return !this.getRange().intersection(r.getRange()).isEmpty();
    }

    default boolean contains(FinderResult r) {
        // We don't want an item to contain itself
        return this.getRange().encloses(r.getRange()) && !this.getRange().equals(r.getRange());
    }
}
