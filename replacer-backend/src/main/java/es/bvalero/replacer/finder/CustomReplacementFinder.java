package es.bvalero.replacer.finder;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
class CustomReplacementFinder extends ReplacementFinder {

    public List<ArticleReplacement> findReplacements(String text, String replacement, String suggestion) {
        String regex = startsWithLowerCase(replacement) && startsWithLowerCase(suggestion)
                ? setFirstUpperCaseClass(replacement)
                : replacement;
        return findMatches(text, regex).stream().map(match -> ArticleReplacement.builder()
                .type(ReplacementFinderService.CUSTOM_FINDER_TYPE)
                .subtype(replacement)
                .start(match.getStart())
                .text(match.getText())
                .suggestions(Collections.singletonList(ReplacementSuggestion.ofNoComment(
                        getNewSuggestion(match.getText(), replacement, suggestion))))
                .build())
                .collect(Collectors.toList());
    }

    private List<MatchResult> findMatches(String text, String regex) {
        List<MatchResult> matches = new ArrayList<>(100);
        RunAutomaton automaton = new RunAutomaton(new RegExp(regex).toAutomaton());
        AutomatonMatcher m = automaton.newMatcher(text);
        while (m.find()) {
            if (isWordCompleteInText(m.start(), m.group(), text)) {
                matches.add(new MatchResult(m.start(), m.group()));
            }
        }
        return matches;
    }

    private String getNewSuggestion(String text, String replacement, String suggestion) {
        if (!text.equalsIgnoreCase(replacement)) {
            throw new IllegalArgumentException(String.format("Text found and replacement don't match: %s - %s",
                    text, replacement));
        } else if (text.equals(replacement)) {
            return suggestion;
        } else {
            return setFirstUpperCase(suggestion);
        }
    }

}
