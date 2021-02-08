package es.bvalero.replacer.finder.common;

import es.bvalero.replacer.page.IndexablePage;
import java.util.List;
import java.util.regex.MatchResult;
import org.apache.commons.collections4.IterableUtils;
import org.jetbrains.annotations.TestOnly;

public interface Finder<T extends FinderResult> {
    // This method returns an Iterable in case we want to retrieve the results one-by-one,
    // for instance to improve performance.
    default Iterable<T> find(IndexablePage page) {
        // The finding process consists basically in three steps:
        // 1. Find a list of all potential match results
        // 2. Validate each match result (by itself and/or against the complete text)
        //    and filter the list to keep only the valid match results
        // 3. Convert the list of match results into a list of T items

        String text = page.getContent();
        Iterable<MatchResult> allMatchResults = findMatchResults(page);
        Iterable<MatchResult> validMatchResults = filterValidMatchResults(allMatchResults, text);
        return convertMatchResults(validMatchResults);
    }

    Iterable<MatchResult> findMatchResults(IndexablePage page);

    default Iterable<MatchResult> filterValidMatchResults(Iterable<MatchResult> matchResults, String completeText) {
        return IterableUtils.filteredIterable(matchResults, matchResult -> validate(matchResult, completeText));
    }

    default boolean validate(MatchResult matchResult, String completeText) {
        // By default return true in case no validation is needed
        return true;
    }

    default Iterable<T> convertMatchResults(Iterable<MatchResult> matchResults) {
        return IterableUtils.transformedIterable(matchResults, this::convert);
    }

    T convert(MatchResult matchResult);

    @TestOnly
    default List<T> findList(String text) {
        return IterableUtils.toList(this.find(FakePage.of(text)));
    }
}
