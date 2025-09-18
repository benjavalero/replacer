package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.*;
import es.bvalero.replacer.finder.StandardType;
import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Independent service that finds all the replacements in a given text.
 * It is composed by several specific replacement finders: misspelling, date format, etc.
 * which implement the same interface.
 * The service applies all these specific finders and returns the collected results.
 */
@Slf4j
@Service
class ReplacementFinderService
    extends ReplacementFinderAbstractService
    implements FinderService<Replacement>, ReplacementFindApi {

    // Dependency injection
    private final List<ReplacementFinder> replacementFinders;

    public ReplacementFinderService(List<ReplacementFinder> replacementFinders, ImmutableFindApi immutableFindApi) {
        super(immutableFindApi);
        this.replacementFinders = replacementFinders;
    }

    @PostConstruct
    public void sortReplacementFinders() {
        Collections.sort(this.replacementFinders);
    }

    @Override
    public Collection<Finder<Replacement>> getFinders() {
        return new ArrayList<>(this.replacementFinders);
    }

    @Override
    public SortedSet<Replacement> findReplacements(FinderPage page) {
        // There will usually be much more immutables found than results.
        // Thus, it is better to obtain first all the results, and then obtain the immutables one by one,
        // aborting in case the replacement list gets empty. This way we can avoid lots of immutable calculations.
        SortedSet<Replacement> allResults = this.find(page);
        return super.filterResults(page, allResults);
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

    //region Implementation without suggestions

    /* Particular case to avoid calculating suggestions when indexing to improve a little the performance */
    @Override
    public SortedSet<Replacement> findReplacementsWithoutSuggestions(FinderPage page) {
        SortedSet<Replacement> allResults = this.findWithoutSuggestions(page);
        return super.filterResults(page, allResults);
    }

    private SortedSet<Replacement> findWithoutSuggestions(FinderPage page) {
        final SortedSet<Replacement> results = new ConcurrentSkipListSet<>();
        findStreamWithoutSuggestions(page).forEach(r -> {
            if (!results.add(r)) {
                LOGGER.warn("Duplicated finder result: {}", r);
            }
        });
        return results;
    }

    private Stream<Replacement> findStreamWithoutSuggestions(FinderPage page) {
        return findStreamWithoutSuggestions(page, getFinders());
    }

    private Stream<Replacement> findStreamWithoutSuggestions(FinderPage page, Collection<Finder<Replacement>> finders) {
        return finders
            .stream()
            .map(finder -> (ReplacementFinder) finder)
            .flatMap(finder -> finder.findWithNoSuggestions(page));
    }
    //endregion
}
