package es.bvalero.replacer.finder;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.jetbrains.annotations.TestOnly;

public interface Finder<T extends FinderResult> extends Comparable<Finder<T>> {
    // This method returns a stream in case we want to retrieve the result one-by-one,
    // for instance, to improve performance.
    default Stream<T> find(FinderPage page) {
        // The finding process consists basically in three steps:
        // 1. Find a list of all potential match results
        // 2. Validate each match result (by itself and/or against the complete text)
        //    and filter the list to keep only the valid match results
        // 3. Convert the list of match results into a list of T items

        final Stream<MatchResult> validMatchResults = findAndFilter(page);
        return convertMatchResults(validMatchResults, page);
    }

    default Stream<MatchResult> findAndFilter(FinderPage page) {
        final Stream<MatchResult> allMatchResults = findMatchResults(page);
        return filterValidMatchResults(allMatchResults, page);
    }

    Stream<MatchResult> findMatchResults(FinderPage page);

    private Stream<MatchResult> filterValidMatchResults(Stream<MatchResult> matchResults, FinderPage page) {
        return matchResults.filter(matchResult -> validate(matchResult, page));
    }

    default boolean validate(MatchResult matchResult, FinderPage page) {
        // By default, return true in case no validation is needed.
        return true;
    }

    private Stream<T> convertMatchResults(Stream<MatchResult> matchResults, FinderPage page) {
        return matchResults.map(m -> convert(m, page));
    }

    T convert(MatchResult matchResult, FinderPage page);

    @TestOnly
    default List<T> findList(String text) {
        // When testing, validate the position of the match results.
        // If in the future we want to use the parser approach, we can just wrap the FinderPage in a FinderParserPage.
        return find(FinderPage.of(text)).filter(m -> m.validate(text)).toList();
    }

    default FinderPriority getPriority() {
        return FinderPriority.NONE;
    }

    @Override
    default int compareTo(Finder finder) {
        return Integer.compare(finder.getPriority().getValue(), getPriority().getValue());
    }
}
