package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.*;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

public interface ReplacementFinder extends Finder<Replacement> {
    @Override
    default FinderPriority getPriority() {
        return FinderPriority.MEDIUM;
    }

    /* Particular case to avoid calculating suggestions when indexing to improve a little the performance */
    default Stream<Replacement> findWithNoSuggestions(FinderPage page) {
        final Stream<MatchResult> validMatchResults = findAndFilter(page);
        return validMatchResults.map(m -> convertWithNoSuggestions(m, page));
    }

    /** If not overridden, take the replacement with suggestions from default conversion and remove the suggestions. */
    default Replacement convertWithNoSuggestions(MatchResult matchResult, FinderPage page) {
        final Replacement withSuggestions = convert(matchResult, page);
        final Replacement withoutSuggestions = withSuggestions.withNoSuggestions();
        assert withoutSuggestions.equals(withSuggestions);
        assert withoutSuggestions.getSuggestions().isEmpty();
        return withoutSuggestions;
    }

    default Optional<StandardType> findMatchingReplacementType(
        WikipediaLanguage lang,
        String replacement,
        boolean caseSensitive
    ) {
        return Optional.empty();
    }
}
