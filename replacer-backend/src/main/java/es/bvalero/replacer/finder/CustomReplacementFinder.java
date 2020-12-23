package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.page.IndexablePage;
import es.bvalero.replacer.replacement.ReplacementEntity;
import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;

public class CustomReplacementFinder implements ReplacementFinder {

    private final String replacement;
    private final String suggestion;
    private final RunAutomaton automaton;

    public CustomReplacementFinder(String replacement, String suggestion) {
        this.replacement = replacement;
        this.suggestion = suggestion;
        this.automaton = buildCustomRegex(this.replacement, this.suggestion);
    }

    @Override
    public Iterable<Replacement> find(IndexablePage page) {
        return new RegexIterable<>(page, automaton, this::convertMatch, this::isValidMatch);
    }

    private RunAutomaton buildCustomRegex(String replacement, String suggestion) {
        String regex = FinderUtils.startsWithLowerCase(replacement) && FinderUtils.startsWithLowerCase(suggestion)
            ? FinderUtils.setFirstUpperCaseClass(replacement)
            : replacement;
        String escapedRegex = FinderUtils.escapeRegexCharacters(regex);
        return new RunAutomaton(new RegExp(escapedRegex).toAutomaton());
    }

    private Replacement convertMatch(MatchResult matcher) {
        int start = matcher.start();
        String text = matcher.group();
        return Replacement
            .builder()
            .type(ReplacementEntity.TYPE_CUSTOM)
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
