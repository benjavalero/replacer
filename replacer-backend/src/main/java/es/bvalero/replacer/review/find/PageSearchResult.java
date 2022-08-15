package es.bvalero.replacer.review.find;

import java.util.Collection;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeSet;
import lombok.AccessLevel;
import lombok.Getter;

// It's not worth to make this class immutable.
// Instead, we encapsulate the logic to manage the internal list.
final class PageSearchResult {

    // Total number of results no matter the size of the list or the offset
    @Getter(AccessLevel.PACKAGE)
    private int total;

    // Cached IDs of pages to review
    // Sort the cached pages to keep some kind of order especially when skipping pages
    private final NavigableSet<Integer> pageIds = new TreeSet<>();

    // Offset in case there are more total results than the pagination size
    // in order to keep the offset during different iterations while finding a review
    @Getter(AccessLevel.PACKAGE)
    private int offset;

    private PageSearchResult(int total, int offset) {
        this.total = total;
        this.offset = offset;
    }

    static PageSearchResult of(int total, Collection<Integer> pageIds, int offset) {
        PageSearchResult result = new PageSearchResult(total, offset);
        result.addPageIds(pageIds);
        return result;
    }

    static PageSearchResult of(int total, Collection<Integer> pageIds) {
        return PageSearchResult.of(total, pageIds, 0);
    }

    private void addPageIds(Collection<Integer> pageIds) {
        this.pageIds.addAll(pageIds);
    }

    int getSize() {
        return this.pageIds.size();
    }

    static PageSearchResult ofEmpty() {
        return new PageSearchResult(0, 0);
    }

    boolean isEmptyTotal() {
        return total == 0;
    }

    /** @return if the cached result list is empty. It doesn't mean the total is 0! */
    boolean isEmpty() {
        return this.getSize() == 0;
    }

    synchronized Optional<Integer> popPageId() {
        if (this.isEmpty()) {
            this.resetOffset();
            return Optional.empty();
        } else {
            this.total--;
            return Optional.ofNullable(this.pageIds.pollFirst());
        }
    }

    void resetOffset() {
        this.offset = 0;
    }

    @Override
    public String toString() {
        return "PageSearchResult(total=" + this.total + ", pageIds=" + this.getSize() + ")";
    }
}
