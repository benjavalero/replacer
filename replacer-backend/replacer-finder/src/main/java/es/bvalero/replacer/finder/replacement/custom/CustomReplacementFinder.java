package es.bvalero.replacer.finder.replacement.custom;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementKind;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.replacement.finders.MisspellingFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/** Special case of Replacement Finder where the options are set at runtime */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class CustomReplacementFinder extends MisspellingFinder implements ReplacementFinder {

    private final CustomMisspelling customMisspelling;
    private final Pattern pattern;

    static CustomReplacementFinder of(String replacement, boolean caseSensitive, String comment) {
        return new CustomReplacementFinder(
            CustomMisspelling.of(replacement, caseSensitive, comment),
            buildCustomRegex(replacement, caseSensitive)
        );
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), this.pattern);
    }

    private static Pattern buildCustomRegex(String replacement, boolean caseSensitive) {
        if (caseSensitive) {
            return Pattern.compile(Pattern.quote(replacement));
        } else {
            return Pattern.compile(Pattern.quote(replacement), Pattern.CASE_INSENSITIVE);
        }
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), page.getContent());
    }

    @Override
    public Replacement convert(MatchResult matcher, FinderPage page) {
        final int start = matcher.start();
        final String text = matcher.group();
        return Replacement.of(
            start,
            text,
            this.customMisspelling.toCustomType(),
            findSuggestions(text),
            page.getContent()
        );
    }

    private List<Suggestion> findSuggestions(String text) {
        return applyMisspellingSuggestions(text, this.customMisspelling);
    }

    @Override
    protected ReplacementKind getType() {
        return ReplacementKind.CUSTOM;
    }
}
