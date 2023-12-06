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
    extends ReplacementFinderAbstractService
    implements FinderService<Replacement>, ReplacementFindService, ReplacementTypeFindService {

    // Dependency injection
    private final List<ReplacementFinder> replacementFinders;

    public ReplacementFinderService(
        List<ReplacementFinder> replacementFinders,
        ImmutableFinderService immutableFinderService
    ) {
        super(immutableFinderService);
        this.replacementFinders = replacementFinders;
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
}
