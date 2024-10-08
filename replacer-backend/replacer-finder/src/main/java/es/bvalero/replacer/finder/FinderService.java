package es.bvalero.replacer.finder;

import java.util.Set;
import java.util.TreeSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;

public interface FinderService<T extends FinderResult> {
    @Slf4j
    final class LogHolder {
        // Trick to be able to log in interfaces
    }

    /* Default service method returning a sorted set of results */
    default Set<T> find(FinderPage page) {
        // Build the sorted set from the results of the more generic iterable finder
        final Set<T> results = new TreeSet<>();
        findIterable(page).forEach(r -> {
            if (!results.add(r)) {
                FinderService.LogHolder.LOGGER.warn("Duplicated finder result: {}", r);
            }
        });
        return results;
    }

    /*
     * Returns an iterable results in case we want to retrieve the results one-by-one,
     * for instance to improve performance.
     */
    default Iterable<T> findIterable(FinderPage page) {
        // We include a default implementation that just creates an iterable
        // from all the results for each associated finder.
        return findIterable(page, getFinders());
    }

    @SuppressWarnings("unchecked")
    default Iterable<T> findIterable(FinderPage page, Iterable<Finder<T>> finders) {
        // The finders are sorted in the implementations
        // in order not to sort them here every time
        return IterableUtils.chainedIterable(
            IterableUtils.toList(finders).stream().map(finder -> finder.find(page)).toArray(Iterable[]::new)
        );
    }

    /* Finders whose results will be included in the results  */
    Iterable<Finder<T>> getFinders();
}
