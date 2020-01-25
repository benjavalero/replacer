package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder2.RegexIterable;
import es.bvalero.replacer.finder2.ReplacementFindService;
import es.bvalero.replacer.finder2.ReplacementFinder;
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
    public Iterable<Replacement> find(String text) {
        return new RegexIterable<Replacement>(text, automaton, this::convertMatch, this::isValidMatch);
    }

    private RunAutomaton buildCustomRegex(String replacement, String suggestion) {
        String regex = FinderUtils.startsWithLowerCase(replacement) && FinderUtils.startsWithLowerCase(suggestion)
            ? FinderUtils.setFirstUpperCaseClass(replacement)
            : replacement;
        return new RunAutomaton(new RegExp(regex).toAutomaton());
    }

    private Replacement convertMatch(MatchResult matcher) {
        int start = matcher.start();
        String text = matcher.group();
        return Replacement
            .builder()
            .type(ReplacementFindService.CUSTOM_FINDER_TYPE)
            .subtype(this.replacement)
            .start(start)
            .text(text)
            .suggestions(findSuggestions(text))
            .build();
    }

    public List<Suggestion> findSuggestions(String text) {
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
