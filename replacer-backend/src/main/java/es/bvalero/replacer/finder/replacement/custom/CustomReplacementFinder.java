package es.bvalero.replacer.finder.replacement.custom;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.replacement.ReplacementSuggestion;
import es.bvalero.replacer.finder.replacement.finders.MisspellingFinder;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.TestOnly;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
class CustomReplacementFinder implements ReplacementFinder {

    private final String replacement;
    private final boolean caseSensitive;
    private final String suggestion;
    private final Pattern pattern;

    static CustomReplacementFinder of(CustomOptions customOptions) {
        return of(customOptions.getReplacement(), customOptions.isCaseSensitive(), customOptions.getSuggestion());
    }

    @TestOnly
    static CustomReplacementFinder of(String replacement, boolean caseSensitive, String suggestion) {
        return new CustomReplacementFinder(
            replacement,
            caseSensitive,
            suggestion,
            buildCustomRegex(replacement, caseSensitive)
        );
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), pattern);
    }

    private static Pattern buildCustomRegex(String replacement, boolean caseSensitive) {
        if (caseSensitive) {
            return Pattern.compile(Pattern.quote(replacement));
        } else {
            return Pattern.compile(Pattern.quote(replacement), Pattern.CASE_INSENSITIVE);
        }
    }

    @Override
    public Replacement convert(MatchResult matcher, FinderPage page) {
        int start = matcher.start();
        String text = matcher.group();
        return Replacement
            .builder()
            .type(ReplacementType.of(ReplacementKind.CUSTOM, this.replacement))
            .start(start)
            .text(text)
            .suggestions(findSuggestions(text))
            .build();
    }

    private List<ReplacementSuggestion> findSuggestions(String text) {
        CustomMisspelling misspelling = CustomMisspelling.of(replacement, caseSensitive, suggestion);
        return MisspellingFinder.applyMisspellingSuggestions(text, misspelling);
    }
}
