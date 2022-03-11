package es.bvalero.replacer.finder;

import java.util.Set;
import java.util.TreeSet;

import es.bvalero.replacer.common.domain.FinderResult;
import es.bvalero.replacer.common.domain.WikipediaPage;
import org.apache.commons.collections4.IterableUtils;

public interface FinderService<T extends FinderResult> {
    /* Default service method returning a sorted set of results */
    default Set<T> find(WikipediaPage page) {
        // Build the sorted set from the results of the more generic iterable finder
        Set<T> results = new TreeSet<>();
        findIterable(page).forEach(results::add);
        return results;
    }

    /*
     * Returns an iterable results in case we want to retrieve the results one-by-one,
     * for instance to improve performance.
     */
    default Iterable<T> findIterable(WikipediaPage page) {
        // We include a default implementation that just creates an iterable
        // from all the results for each associated finder.
        return findIterable(page, getFinders());
    }

    @SuppressWarnings("unchecked")
    default Iterable<T> findIterable(WikipediaPage page, Iterable<Finder<T>> finders) {
        return IterableUtils.chainedIterable(
            IterableUtils.toList(finders).stream().map(finder -> finder.find(FinderPageMapper.fromDomain(page))).toArray(Iterable[]::new)
        );
    }

    /* Finders whose results will be included in the results  */
    Iterable<Finder<T>> getFinders();
}
