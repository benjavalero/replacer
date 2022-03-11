package es.bvalero.replacer.finder.replacement.custom;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.Suggestion;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.replacement.finders.MisspellingFinder;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/** Special case of Replacement Finder where the options are set at runtime */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class CustomReplacementFinder implements ReplacementFinder {

    private final String replacement;
    private final boolean caseSensitive;
    private final String suggestion;
    private final Pattern pattern;

    static CustomReplacementFinder of(CustomOptions customOptions) {
        return new CustomReplacementFinder(
            customOptions.getReplacement(),
            customOptions.isCaseSensitive(),
            customOptions.getSuggestion(),
            buildCustomRegex(customOptions.getReplacement(), customOptions.isCaseSensitive())
        );
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
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
    public Replacement convert(MatchResult matcher, WikipediaPage page) {
        final int start = matcher.start();
        final String text = matcher.group();
        return Replacement
            .builder()
            .type(ReplacementType.of(ReplacementKind.CUSTOM, this.replacement))
            .start(start)
            .text(text)
            .suggestions(findSuggestions(text))
            .build();
    }

    private List<Suggestion> findSuggestions(String text) {
        final CustomMisspelling misspelling = CustomMisspelling.of(replacement, caseSensitive, suggestion);
        return MisspellingFinder.applyMisspellingSuggestions(text, misspelling);
    }
}
