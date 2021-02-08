package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import es.bvalero.replacer.page.IndexablePage;
import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

class CustomReplacementFinder implements ReplacementFinder {

    private final String replacement;
    private final String suggestion;
    private final Pattern pattern;

    CustomReplacementFinder(String replacement, String suggestion) {
        this.replacement = replacement;
        this.suggestion = suggestion;
        this.pattern = buildCustomRegex(this.replacement, this.suggestion);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(IndexablePage page) {
        return RegexMatchFinder.find(page.getContent(), pattern);
    }

    private Pattern buildCustomRegex(String replacement, String suggestion) {
        // If both the replacement and the suggestion start with lowercase
        // we consider the regex to be case-insensitive
        boolean caseInsensitive =
            FinderUtils.startsWithLowerCase(replacement) && FinderUtils.startsWithLowerCase(suggestion);
        if (caseInsensitive) {
            return Pattern.compile(Pattern.quote(replacement), Pattern.CASE_INSENSITIVE);
        } else {
            return Pattern.compile(Pattern.quote(replacement));
        }
    }

    @Override
    public Replacement convert(MatchResult matcher) {
        int start = matcher.start();
        String text = matcher.group();
        return Replacement
            .builder()
            .type(ReplacementType.CUSTOM)
            .subtype(this.replacement)
            .start(start)
            .text(text)
            .suggestions(findSuggestions(text))
            .build();
    }

    private List<Suggestion> findSuggestions(String text) {
        return Collections.singletonList(
            Suggestion.ofNoComment(getNewSuggestion(text, this.replacement, this.suggestion))
        );
    }

    private String getNewSuggestion(String text, String replacement, String suggestion) {
        if (!text.equalsIgnoreCase(replacement)) {
            throw new IllegalArgumentException(
                String.format("Text found and replacement don't match: %s - %s", text, replacement)
            );
        } else if (text.equals(replacement)) {
            return suggestion;
        } else {
            return FinderUtils.setFirstUpperCase(suggestion);
        }
    }
}
