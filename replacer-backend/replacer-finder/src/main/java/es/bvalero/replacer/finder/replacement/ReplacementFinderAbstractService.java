package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFindApi;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.parser.FinderParserPage;
import java.util.SortedSet;
import java.util.stream.Stream;

public abstract class ReplacementFinderAbstractService {

    // Dependency injection
    private final ImmutableFindApi immutableFindApi;

    public ReplacementFinderAbstractService(ImmutableFindApi immutableFindApi) {
        this.immutableFindApi = immutableFindApi;
    }

    protected SortedSet<Replacement> filterResults(FinderPage page, SortedSet<Replacement> allResults) {
        // We assume the collection of results is a mutable set sorted and of course with no duplicates

        // Remove nested. There might be replacements (strictly) contained in others.
        removeNested(allResults);

        // Remove the ones contained in immutables
        return removeImmutables(page, allResults);
    }

    private void removeNested(SortedSet<Replacement> results) {
        // We need to filter the items against the collection itself, so it is not a stateless predicate.
        // We assume all the results in the iterable are distinct, in this case,
        // this means there are not two results with the same start and end,
        // so the contain function is strict.
        Replacement.removeNested(results);
    }

    private SortedSet<Replacement> removeImmutables(FinderPage page, final SortedSet<Replacement> resultList) {
        // No need to find the immutables if there are no results
        if (resultList.isEmpty()) {
            return resultList;
        }

        findImmutables(page)
            .takeWhile(immutable -> !resultList.isEmpty())
            .forEach(immutable -> resultList.removeIf(immutable::contains));

        return resultList;
    }

    private Stream<Immutable> findImmutables(FinderPage page) {
        final FinderParserPage parserPage = FinderParserPage.of(page);
        return immutableFindApi.findImmutables(parserPage);
    }
}
