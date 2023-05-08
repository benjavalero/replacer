package es.bvalero.replacer.finder;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinderService;
import es.bvalero.replacer.finder.immutable.ImmutableFinderService;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.finder.replacement.custom.CustomReplacementFinderService;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Service;

@Service
class ResultFinder
    implements CosmeticFindService, ReplacementFindService, CustomReplacementFindService, ReplacementTypeMatchService {

    @Autowired
    private CosmeticFinderService cosmeticFinderService;

    @Autowired
    private ReplacementFinderService replacementFinderService;

    @Autowired
    private ImmutableFinderService immutableFinderService;

    @Autowired
    private CustomReplacementFinderService customReplacementFinderService;

    @Override
    public Collection<Cosmetic> findCosmetics(FinderPage page) {
        return cosmeticFinderService.find(page);
    }

    @Loggable(value = LogLevel.TRACE, skipResult = true, warnOver = 1, warnUnit = TimeUnit.SECONDS)
    @Override
    public Collection<Replacement> findReplacements(FinderPage page) {
        // There will usually be much more immutables found than results.
        // Thus, it is better to obtain first all the results, and then obtain the immutables one by one,
        // aborting in case the replacement list gets empty. This way we can avoid lots of immutable calculations.
        Collection<Replacement> allResults = replacementFinderService.find(page);
        return filterResults(page, allResults);
    }

    private Iterable<Immutable> findImmutables(FinderPage page) {
        return immutableFinderService.findIterable(page);
    }

    @Override
    public Collection<Replacement> findCustomReplacements(FinderPage page, CustomMisspelling customMisspelling) {
        // There will usually be much more immutables found than results.
        // Thus, it is better to obtain first all the results, and then obtain the immutables one by one,
        // aborting in case the replacement list gets empty. This way we can avoid lots of immutable calculations.
        Iterable<Replacement> customResults = customReplacementFinderService.findCustomReplacements(
            page,
            customMisspelling
        );
        Set<Replacement> sortedResults = new TreeSet<>();
        customResults.forEach(sortedResults::add);
        return filterResults(page, sortedResults);
    }

    private Collection<Replacement> filterResults(FinderPage page, Collection<Replacement> allResults) {
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
            return Collections.emptyList();
        }

        for (Immutable immutable : findImmutables(page)) {
            resultList.removeIf(immutable::contains);

            // No need to continue finding the immutables if there are no results
            if (resultList.isEmpty()) {
                return Collections.emptyList();
            }
        }

        return resultList;
    }

    @Override
    public Optional<StandardType> findMatchingReplacementType(
        WikipediaLanguage lang,
        String replacement,
        boolean caseSensitive
    ) {
        return replacementFinderService.findMatchingReplacementType(lang, replacement, caseSensitive);
    }
}
