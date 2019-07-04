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
        return findMatches(text, replacement).stream().map(match -> new ArticleReplacement(
                match.getText(),
                match.getStart(),
                ReplacementFinderService.CUSTOM_FINDER_TYPE,
                replacement,
                Collections.singletonList(new ReplacementSuggestion(suggestion, null))))
                .collect(Collectors.toList());
    }

    private List<MatchResult> findMatches(String text, String replacement) {
        List<MatchResult> matches = new ArrayList<>(100);
        RunAutomaton automaton = new RunAutomaton(new RegExp(replacement).toAutomaton());
        AutomatonMatcher m = automaton.newMatcher(text);
        while (m.find()) {
            if (isWordCompleteInText(m.start(), m.group(), text)) {
                matches.add(new MatchResult(m.start(), m.group()));
            }
        }
        return matches;
    }

}
