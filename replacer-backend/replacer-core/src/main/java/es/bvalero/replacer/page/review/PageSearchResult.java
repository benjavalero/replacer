package es.bvalero.replacer.page.review;

import es.bvalero.replacer.page.PageKey;
import java.util.*;
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
    private final NavigableSet<PageKey> pageKeys = new TreeSet<>();

    // Offset in case there are more total results than the pagination size
    // in order to keep the offset during different iterations while finding a review
    @Getter(AccessLevel.PACKAGE)
    private int offset;

    private PageSearchResult(int total, int offset) {
        this.total = total;
        this.offset = offset;
    }

    static PageSearchResult of(int total, Collection<PageKey> pageKeys, int offset) {
        PageSearchResult result = new PageSearchResult(total, offset);
        result.addPageKeys(pageKeys);
        return result;
    }

    static PageSearchResult of(int total, Collection<PageKey> pageKeys) {
        return PageSearchResult.of(total, pageKeys, 0);
    }

    private void addPageKeys(Collection<PageKey> pageKeys) {
        this.pageKeys.addAll(pageKeys);
    }

    int getSize() {
        return this.pageKeys.size();
    }

    static PageSearchResult ofEmpty() {
        return new PageSearchResult(0, 0);
    }

    boolean isEmptyTotal() {
        return this.total == 0;
    }

    /** @return if the cached result list is empty. It doesn't mean the total is 0! */
    boolean isEmpty() {
        return getSize() == 0;
    }

    synchronized Optional<PageKey> popPageKey() {
        if (isEmpty()) {
            resetOffset();
            return Optional.empty();
        } else {
            this.total--;
            assert this.total >= 0;
            return Optional.of(Objects.requireNonNull(this.pageKeys.pollFirst()));
        }
    }

    void resetOffset() {
        this.offset = 0;
    }

    @Override
    public String toString() {
        return "PageSearchResult(total=" + this.total + ", pageKeys=" + getSize() + ")";
    }
}
