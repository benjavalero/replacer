package es.bvalero.replacer.finder.replacement;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderResult;
import es.bvalero.replacer.finder.FinderService;
import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableFinderService;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;

/** Decorator to remove the found items contained in immutables */
public abstract class ImmutableFilterFinderService<T extends FinderResult> implements FinderService<T> {

    private ImmutableFinderService immutableFinderService;

    @Autowired
    public final void setImmutableFinderService(ImmutableFinderService immutableFinderService) {
        this.immutableFinderService = immutableFinderService;
    }

    @Loggable(prepend = true, value = Loggable.TRACE, unit = TimeUnit.SECONDS)
    @Override
    public Iterable<T> findIterable(FinderPage page) {
        // There will usually be much more immutables found than results.
        // Thus, it is better to obtain first all the results, and then obtain the immutables one by one,
        // aborting in case the replacement list gets empty. This way we can avoid lots of immutable calculations.
        Iterable<T> allResults = FinderService.super.findIterable(page, getFinders());
        return filterResults(page, allResults);
    }

    protected Iterable<T> filterResults(FinderPage page, Iterable<T> allResults) {
        // Remove duplicates. By the way we sort the collection.
        Iterable<T> noDupes = removeDuplicates(allResults);

        // Remove nested. There might be replacements (strictly) contained in others.
        Iterable<T> noNested = removeNested(noDupes);

        // Remove the ones contained in immutables
        return removeImmutables(page, noNested);
    }

    private Iterable<T> removeDuplicates(Iterable<T> results) {
        // TreeSet to distinct and sort
        Set<T> noDupes = new TreeSet<>();
        for (T result : results) {
            noDupes.add(result);
        }
        return noDupes;
    }

    private Iterable<T> removeNested(Iterable<T> results) {
        // We need to filter the items against the collection itself, so it is not a stateless predicate.
        // We assume all the results in the iterable are distinct, in this case,
        // this means there are not two results with the same start and end,
        // so the contain function is strict.

        // Filter to return the results which are NOT strictly contained in any other
        return toStream(results)
            .filter(r -> toStream(results).noneMatch(r2 -> r2.containsStrictly(r)))
            .collect(Collectors.toList());
    }

    private Stream<T> toStream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    private Iterable<T> removeImmutables(FinderPage page, Iterable<T> results) {
        // LinkedList to remove items. Order is kept.
        List<T> resultList = new LinkedList<>(IterableUtils.toList(results));

        // No need to find the immutables if there are no results
        if (resultList.isEmpty()) {
            return Collections.emptyList();
        }

        for (Immutable immutable : immutableFinderService.findIterable(page)) {
            resultList.removeIf(immutable::intersects);

            // No need to continue finding the immutables if there are no results
            if (resultList.isEmpty()) {
                return Collections.emptyList();
            }
        }

        return resultList;
    }
}
