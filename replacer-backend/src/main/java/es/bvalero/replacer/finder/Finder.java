package es.bvalero.replacer.finder;

import java.util.List;
import java.util.regex.MatchResult;

import es.bvalero.replacer.common.domain.FinderResult;
import es.bvalero.replacer.common.domain.WikipediaPage;
import org.apache.commons.collections4.IterableUtils;
import org.jetbrains.annotations.TestOnly;

public interface Finder<T extends FinderResult> {
    // This method returns an Iterable in case we want to retrieve the results one-by-one,
    // for instance to improve performance.
    default Iterable<T> find(WikipediaPage page) {
        // The finding process consists basically in three steps:
        // 1. Find a list of all potential match results
        // 2. Validate each match result (by itself and/or against the complete text)
        //    and filter the list to keep only the valid match results
        // 3. Convert the list of match results into a list of T items

        final Iterable<MatchResult> allMatchResults = findMatchResults(page);
        final Iterable<MatchResult> validMatchResults = filterValidMatchResults(allMatchResults, page);
        return convertMatchResults(validMatchResults, page);
    }

    Iterable<MatchResult> findMatchResults(WikipediaPage page);

    default Iterable<MatchResult> filterValidMatchResults(Iterable<MatchResult> matchResults, WikipediaPage page) {
        return IterableUtils.filteredIterable(matchResults, matchResult -> validate(matchResult, page));
    }

    default boolean validate(MatchResult matchResult, WikipediaPage page) {
        // By default, return true in case no validation is needed.
        return true;
    }

    default Iterable<T> convertMatchResults(Iterable<MatchResult> matchResults, WikipediaPage page) {
        return IterableUtils.transformedIterable(matchResults, m -> convert(m, page));
    }

    T convert(MatchResult matchResult, WikipediaPage page);

    @TestOnly
    default List<T> findList(String text) {
        // When testing, validate the position of the match results.
        return IterableUtils.toList(
            IterableUtils.filteredIterable(this.find(WikipediaPage.of(text)), m -> m.validate(text))
        );
    }
}
