package es.bvalero.replacer.page.findreplacement;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.PageReplacement;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPageMapper;
import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableFinderService;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.finder.replacement.ReplacementMapper;
import es.bvalero.replacer.finder.replacement.custom.CustomOptions;
import es.bvalero.replacer.finder.replacement.custom.CustomReplacementFinderService;
import es.bvalero.replacer.page.review.PageReviewOptions;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Service;

@Service
public class FindReplacementsService {

    @Autowired
    private ReplacementFinderService replacementFinderService;

    @Autowired
    private CustomReplacementFinderService customReplacementFinderService;

    @Autowired
    private ImmutableFinderService immutableFinderService;

    /** Find all replacements in the page content ignoring the ones contained in immutables */
    @Loggable(value = LogLevel.TRACE, skipResult = true, warnOver = 1, warnUnit = TimeUnit.SECONDS)
    public Collection<PageReplacement> findReplacements(WikipediaPage page) {
        FinderPage finderPage = FinderPageMapper.fromDomain(page);

        // There will usually be much more immutables found than results.
        // Thus, it is better to obtain first all the results, and then obtain the immutables one by one,
        // aborting in case the replacement list gets empty. This way we can avoid lots of immutable calculations.
        Collection<Replacement> allResults = replacementFinderService.find(finderPage);
        return ReplacementMapper.toDomain(filterResults(finderPage, allResults));
    }

    /** Find all replacements in the page content ignoring the ones contained in immutables */
    @Loggable(value = LogLevel.TRACE, skipResult = true, warnOver = 1, warnUnit = TimeUnit.SECONDS)
    public Collection<PageReplacement> findCustomReplacements(WikipediaPage page, PageReviewOptions options) {
        FinderPage finderPage = FinderPageMapper.fromDomain(page);

        // There will usually be much more immutables found than results.
        // Thus, it is better to obtain first all the results, and then obtain the immutables one by one,
        // aborting in case the replacement list gets empty. This way we can avoid lots of immutable calculations.
        Iterable<Replacement> allResults = customReplacementFinderService.findCustomReplacements(
            finderPage,
            convertOptions(options)
        );
        return ReplacementMapper.toDomain(filterResults(finderPage, allResults));
    }

    private CustomOptions convertOptions(PageReviewOptions options) {
        String subtype = options.getType().getSubtype();
        Boolean cs = options.getCs();
        String suggestion = options.getSuggestion();
        assert cs != null && suggestion != null;
        return CustomOptions.of(subtype, cs, suggestion);
    }

    private Collection<Replacement> filterResults(FinderPage page, Iterable<Replacement> allResults) {
        // Remove duplicates. By the way we sort the collection.
        Collection<Replacement> noDupes = removeDuplicates(allResults);

        // Remove nested. There might be replacements (strictly) contained in others.
        Collection<Replacement> noNested = removeNested(noDupes);

        // Remove the ones contained in immutables
        return removeImmutables(page, noNested);
    }

    private Collection<Replacement> removeDuplicates(Iterable<Replacement> results) {
        // TreeSet to distinct and sort
        Set<Replacement> noDupes = new TreeSet<>();
        for (Replacement result : results) {
            noDupes.add(result);
        }
        return noDupes;
    }

    private Collection<Replacement> removeNested(Collection<Replacement> results) {
        // We need to filter the items against the collection itself, so it is not a stateless predicate.
        // We assume all the results in the iterable are distinct, in this case,
        // this means there are not two results with the same start and end,
        // so the contain function is strict.

        // Filter to return the results which are NOT strictly contained in any other
        return results
            .stream()
            .filter(r -> results.stream().noneMatch(r2 -> r2.containsStrictly(r)))
            .collect(Collectors.toUnmodifiableList());
    }

    private Collection<Replacement> removeImmutables(FinderPage page, Collection<Replacement> results) {
        // LinkedList to remove items. Order is kept.
        List<Replacement> resultList = new LinkedList<>(results);

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
