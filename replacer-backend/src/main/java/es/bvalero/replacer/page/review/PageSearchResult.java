package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.WikipediaPage;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;

// It's not worth to make this class immutable.
// Instead we encapsulate the logic to manage the internal list.
final class PageSearchResult {

    // Total number of results no matter the size of the list or the offset
    @Getter(AccessLevel.PACKAGE)
    private int total;

    // Cached IDs of pages to review
    // We need a List in order to use "removeIf"
    private final List<Integer> pageIds = new LinkedList<>();

    // Cached pages to review
    private final Map<Integer, WikipediaPage> pageCache = new HashMap<>();

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

    Collection<Integer> getPageIds() {
        return this.pageIds.stream().collect(Collectors.toUnmodifiableList());
    }

    int getSize() {
        return this.pageIds.size();
    }

    void addCachedPages(Collection<WikipediaPage> pages) {
        pages.forEach(p -> this.pageCache.put(p.getId().getPageId(), p));
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
            this.pageCache.clear();
            this.resetOffset();
            return Optional.empty();
        } else {
            this.total--;
            return Optional.of(this.pageIds.remove(0));
        }
    }

    void resetOffset() {
        this.offset = 0;
    }

    Optional<WikipediaPage> getCachedPage(int pageId) {
        return Optional.ofNullable(this.pageCache.remove(pageId));
    }

    @Override
    public String toString() {
        return "PageSearchResult(total=" + this.total + ", pageIds=" + this.getSize() + ")";
    }
}
