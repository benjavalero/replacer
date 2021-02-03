package es.bvalero.replacer.wikipedia;

import java.util.*;
import lombok.Getter;

// It's not worth to make this class immutable.
// At least we encapsulate the logic to manage the internal list.
@Getter
public final class PageSearchResult {

    private final List<Integer> pageIds = new LinkedList<>();
    private long total;

    private PageSearchResult(long total, List<Integer> pageIds) {
        this.total = total;
        // Add a set to remove duplicated page ids
        // We need a List in order to use "removeIf"
        this.pageIds.addAll(new HashSet<>(pageIds));
    }

    public static PageSearchResult of(long total, List<Integer> pageIds) {
        return new PageSearchResult(total, pageIds);
    }

    public static PageSearchResult ofEmpty() {
        return new PageSearchResult(0, Collections.emptyList());
    }

    public boolean isEmpty() {
        return this.getPageIds().isEmpty();
    }

    public Optional<Integer> popPageId() {
        if (isEmpty()) {
            return Optional.empty();
        } else {
            this.total--;
            return Optional.of(this.pageIds.remove(0));
        }
    }

    public void removePageIds(List<Integer> toRemove) {
        toRemove.forEach(
            id -> {
                if (this.pageIds.remove(id)) {
                    this.total--;
                }
            }
        );
    }

    @Override
    public String toString() {
        return "PageSearchResult(total=" + this.getTotal() + ", pageIds=" + this.getPageIds().size() + ")";
    }
}
