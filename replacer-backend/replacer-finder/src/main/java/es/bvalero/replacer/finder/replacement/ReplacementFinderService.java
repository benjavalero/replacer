package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.*;
import es.bvalero.replacer.finder.immutable.ImmutableFinderService;
import java.util.*;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

/**
 * Independent service that finds all the replacements in a given text.
 * It is composed by several specific replacement finders: misspelling, date format, etc.
 * which implement the same interface.
 * The service applies all these specific finders and returns the collected results.
 */
@Service
public class ReplacementFinderService
    implements FinderService<Replacement>, ReplacementFindService, ReplacementTypeFindService {

    // Dependency injection
    private final List<ReplacementFinder> replacementFinders;
    private final ImmutableFinderService immutableFinderService;

    public ReplacementFinderService(
        List<ReplacementFinder> replacementFinders,
        ImmutableFinderService immutableFinderService
    ) {
        this.replacementFinders = replacementFinders;
        this.immutableFinderService = immutableFinderService;
    }

    @PostConstruct
    public void sortReplacementFinders() {
        Collections.sort(this.replacementFinders);
    }

    @Override
    public Iterable<Finder<Replacement>> getFinders() {
        return new ArrayList<>(this.replacementFinders);
    }

    @Override
    public Collection<Replacement> findReplacements(FinderPage page) {
        // There will usually be much more immutables found than results.
        // Thus, it is better to obtain first all the results, and then obtain the immutables one by one,
        // aborting in case the replacement list gets empty. This way we can avoid lots of immutable calculations.
        Collection<Replacement> allResults = this.find(page);
        return filterResults(page, allResults);
    }

    protected Collection<Replacement> filterResults(FinderPage page, Collection<Replacement> allResults) {
        // We assume the collection of results is a mutable set sorted and of course with no duplicates

        // Remove nested. There might be replacements (strictly) contained in others.
        removeNested(allResults);

        // Remove the ones contained in immutables
        return removeImmutables(page, allResults);
    }

    private void removeNested(Collection<Replacement> results) {
        // We need to filter the items against the collection itself, so it is not a stateless predicate.
        // We assume all the results in the iterable are distinct, in this case,
        // this means there are not two results with the same start and end,
        // so the contain function is strict.
        Replacement.removeNested(results);
    }

    private Collection<Replacement> removeImmutables(FinderPage page, Collection<Replacement> resultList) {
        // No need to find the immutables if there are no results
        if (resultList.isEmpty()) {
            return List.of();
        }

        for (Immutable immutable : findImmutables(page)) {
            resultList.removeIf(immutable::contains);

            // No need to continue finding the immutables if there are no results
            if (resultList.isEmpty()) {
                return List.of();
            }
        }

        return resultList;
    }

    private Iterable<Immutable> findImmutables(FinderPage page) {
        return immutableFinderService.findIterable(page);
    }

    @Override
    public Optional<StandardType> findReplacementType(
        WikipediaLanguage lang,
        String replacement,
        boolean caseSensitive
    ) {
        return this.replacementFinders.stream()
            .map(finder -> finder.findMatchingReplacementType(lang, replacement, caseSensitive))
            .filter(Optional::isPresent)
            .findAny()
            .orElse(Optional.empty());
    }
}
