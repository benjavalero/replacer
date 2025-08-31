package es.bvalero.replacer.finder;

import java.util.Collection;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

public interface FinderService<T extends FinderResult> {
    @Slf4j
    final class LogHolder {
        // Trick to be able to log in interfaces
    }

    /* Default service method returning a sorted set of results */
    default SortedSet<T> find(FinderPage page) {
        // Build the sorted set from the results of the more generic stream finder.
        // We return a mutable and thread-safe collection
        // as we will remove items if they are contained in immutables.
        final SortedSet<T> results = new ConcurrentSkipListSet<>();
        findStream(page).forEach(r -> {
            if (!results.add(r)) {
                FinderService.LogHolder.LOGGER.warn("Duplicated finder result: {}", r);
            }
        });
        return results;
    }

    /*
     * Returns the results as a stream in case we want to retrieve the results one-by-one,
     * for instance to improve performance.
     */
    default Stream<T> findStream(FinderPage page) {
        // We include a default implementation that just creates a stream
        // from all the results for each associated finder.
        return findStream(page, getFinders());
    }

    default Stream<T> findStream(FinderPage page, Collection<Finder<T>> finders) {
        // The finders are sorted in the implementations
        // in order not to sort them here every time.
        // According to the benchmark, iterating the finders in parallel
        // with the default number of threads is almost twice faster.
        // However, running the same benchmark in Production,
        // we find the best option is to keep using the sequential stream.
        return finders.stream().flatMap(finder -> finder.find(page));
    }

    /* Finders whose results will be included in the results  */
    Collection<Finder<T>> getFinders();
}
