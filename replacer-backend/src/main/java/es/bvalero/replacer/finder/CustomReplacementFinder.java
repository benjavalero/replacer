package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder2.ReplacementFindService;
import es.bvalero.replacer.finder2.ReplacementFinder;

import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

public class CustomReplacementFinder implements ReplacementFinder {
    private String replacement;
    private String suggestion;

    public CustomReplacementFinder(String replacement, String suggestion) {
        this.replacement = replacement;
        this.suggestion = suggestion;
    }

    @Override
    public Stream<Replacement> find(String text) {
        RunAutomaton customAutomaton = buildCustomRegex(this.replacement, this.suggestion);
        return findStream(text, customAutomaton, matcher -> convertMatch(matcher));
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
