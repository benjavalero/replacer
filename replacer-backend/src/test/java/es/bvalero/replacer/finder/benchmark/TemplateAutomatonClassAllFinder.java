package es.bvalero.replacer.finder.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.MatchResult;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class TemplateAutomatonClassAllFinder extends TemplateAbstractFinder {

    private static final String REGEX_TEMPLATE = "\\{\\{[^}]+?}}";
    private static RunAutomaton automaton;

    TemplateAutomatonClassAllFinder(List<String> words) {
        Set<String> wordsToJoin = new HashSet<>();
        for (String word : words) {
            if (isLowercase(word)) {
                wordsToJoin.add(setFirstUpperCaseClass(word));
            } else {
                wordsToJoin.add(word);
            }
        }
        automaton = new RunAutomaton(new RegExp(String.format("\\{\\{(%s)[|:](%s|[^}])+?}}", StringUtils.join(wordsToJoin, "|"), REGEX_TEMPLATE)).toAutomaton());
    }

    Set<MatchResult> findMatches(String text) {
        Set<MatchResult> matches = new HashSet<>();
        AutomatonMatcher m = automaton.newMatcher(text);
        while (m.find()) {
            matches.add(new MatchResult(m.start(), m.group()));
        }
        return matches;
    }

}
