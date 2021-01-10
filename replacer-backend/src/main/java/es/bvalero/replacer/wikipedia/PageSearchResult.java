package es.bvalero.replacer.wikipedia;

import java.util.*;
import lombok.Getter;

@Getter
public class PageSearchResult {

    private final List<Integer> pageIds;
    private long total;

    public PageSearchResult(long total, Collection<Integer> pageIds) {
        this.total = total;
        // Add a set to remove duplicated page ids
        // We need a List in order to use "removeIf"
        this.pageIds = new LinkedList<>(new HashSet<>(pageIds));
    }

    public static PageSearchResult ofEmpty() {
        return new PageSearchResult(0, Collections.emptyList());
    }

    public boolean isEmpty() {
        return pageIds.isEmpty();
    }

    public Optional<Integer> popPageId() {
        if (isEmpty()) {
            return Optional.empty();
        } else {
            total--;
            return Optional.of(pageIds.remove(0));
        }
    }

    public void removePageIds(List<Integer> toRemove) {
        toRemove.forEach(
            id -> {
                if (pageIds.remove(id)) {
                    total--;
                }
            }
        );
    }

    public String toString() {
        return "PageSearchResult(total=" + this.getTotal() + ", pageIds=" + this.getPageIds().size() + ")";
    }
}
