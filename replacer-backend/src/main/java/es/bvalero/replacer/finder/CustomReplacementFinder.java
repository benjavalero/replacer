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
class CustomReplacementFinder {

    public List<Replacement> findReplacements(String text, String replacement, String suggestion) {
        String regex = FinderUtils.startsWithLowerCase(replacement) && FinderUtils.startsWithLowerCase(suggestion)
                ? FinderUtils.setFirstUpperCaseClass(replacement)
                : replacement;
        return findMatches(text, regex).stream().map(match -> Replacement.builder()
                .type(ReplacementFinderService.CUSTOM_FINDER_TYPE)
                .subtype(replacement)
                .start(match.getStart())
                .text(match.getText())
                .suggestions(Collections.singletonList(ReplacementSuggestion.ofNoComment(
                        getNewSuggestion(match.getText(), replacement, suggestion))))
                .build())
                .collect(Collectors.toList());
    }

    private List<IgnoredReplacement> findMatches(String text, String regex) {
        List<IgnoredReplacement> matches = new ArrayList<>(100);
        RunAutomaton automaton = new RunAutomaton(new RegExp(regex).toAutomaton());
        AutomatonMatcher m = automaton.newMatcher(text);
        while (m.find()) {
            if (FinderUtils.isWordCompleteInText(m.start(), m.group(), text)) {
                matches.add(IgnoredReplacement.of(m.start(), m.group()));
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
            return FinderUtils.setFirstUpperCase(suggestion);
        }
    }

}
