package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.WikipediaPage;
import java.util.*;
import lombok.Getter;

// It's not worth to make this class immutable.
// At least we encapsulate the logic to manage the internal list.
@Getter
final class PageSearchResult {

    private final List<Integer> pageIds;
    private int total;
    private final Map<Integer, WikipediaPage> pageCache = new HashMap<>(); // Optional to be used on custom search

    private PageSearchResult(int total, Collection<Integer> pageIds) {
        this.total = total;
        // We need a List in order to use "removeIf"
        this.pageIds = new LinkedList<>(pageIds);
    }

    void addCachedPages(Collection<WikipediaPage> pages) {
        pages.forEach(p -> this.pageCache.put(p.getId().getPageId(), p));
    }

    Optional<WikipediaPage> getCachedPage(int pageId) {
        return Optional.ofNullable(this.pageCache.remove(pageId));
    }

    static PageSearchResult of(int total, Collection<Integer> pageIds) {
        return new PageSearchResult(total, pageIds);
    }

    static PageSearchResult ofEmpty() {
        return new PageSearchResult(0, Collections.emptyList());
    }

    boolean isEmptyTotal() {
        return total == 0;
    }

    /** @return if the cached result list is empty. It doesn't mean the total is 0! */
    boolean isEmpty() {
        return this.getPageIds().isEmpty();
    }

    synchronized Optional<Integer> popPageId() {
        if (this.isEmpty()) {
            this.pageCache.clear();
            return Optional.empty();
        } else {
            this.total--;
            return Optional.of(this.pageIds.remove(0));
        }
    }

    @Override
    public String toString() {
        return "PageSearchResult(total=" + this.getTotal() + ", pageIds=" + this.getPageIds().size() + ")";
    }
}
