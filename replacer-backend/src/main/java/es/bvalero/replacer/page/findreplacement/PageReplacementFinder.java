package es.bvalero.replacer.page.findreplacement;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.FinderResult;
import es.bvalero.replacer.common.domain.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableFinderService;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.finder.replacement.custom.CustomReplacementFinderService;
import es.bvalero.replacer.page.review.PageReviewOptions;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Service;

@Service
public class PageReplacementFinder {

    @Autowired
    private ReplacementFinderService replacementFinderService;

    @Autowired
    private CustomReplacementFinderService customReplacementFinderService;

    @Autowired
    private ImmutableFinderService immutableFinderService;

    /** Find all replacements in the page content ignoring the ones contained in immutables */
    @Loggable(value = LogLevel.TRACE, skipResult = true, warnOver = 1, warnUnit = TimeUnit.SECONDS)
    public Collection<Replacement> findReplacements(WikipediaPage page) {
        // There will usually be much more immutables found than results.
        // Thus, it is better to obtain first all the results, and then obtain the immutables one by one,
        // aborting in case the replacement list gets empty. This way we can avoid lots of immutable calculations.
        Set<Replacement> allResults = replacementFinderService.find(page);
        return filterResults(page, allResults);
    }

    /** Find all replacements in the page content ignoring the ones contained in immutables */
    @Loggable(value = LogLevel.TRACE, skipResult = true, warnOver = 1, warnUnit = TimeUnit.SECONDS)
    public Collection<Replacement> findCustomReplacements(WikipediaPage page, PageReviewOptions options) {
        // There will usually be much more immutables found than results.
        // Thus, it is better to obtain first all the results, and then obtain the immutables one by one,
        // aborting in case the replacement list gets empty. This way we can avoid lots of immutable calculations.
        Iterable<Replacement> customResults = customReplacementFinderService.findCustomReplacements(
            page ,options
        );
        Set<Replacement> sortedResults = new TreeSet<>();
        customResults.forEach(sortedResults::add);
        return filterResults(page, sortedResults);
    }

    private Collection<Replacement> filterResults(WikipediaPage page, Collection<Replacement> allResults) {
        // We can assume the collection of results is a mutable set sorted and of course with no duplicates

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
        FinderResult.removeNested(results);
    }

    private Collection<Replacement> removeImmutables(WikipediaPage page, Collection<Replacement> resultList) {
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
