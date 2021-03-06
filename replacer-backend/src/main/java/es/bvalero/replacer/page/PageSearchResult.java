package es.bvalero.replacer.page;

import java.util.*;
import lombok.Getter;

// It's not worth to make this class immutable.
// At least we encapsulate the logic to manage the internal list.
@Getter
final class PageSearchResult {

    private final List<Integer> pageIds = new LinkedList<>();
    private long total;

    private PageSearchResult(long total, List<Integer> pageIds) {
        this.total = total;
        // Add a set to remove duplicated page ids
        // We need a List in order to use "removeIf"
        this.pageIds.addAll(new HashSet<>(pageIds));
    }

    static PageSearchResult of(long total, List<Integer> pageIds) {
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
