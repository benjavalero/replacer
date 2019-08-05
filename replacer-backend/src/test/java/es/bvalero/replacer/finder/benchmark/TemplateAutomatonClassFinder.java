package es.bvalero.replacer.finder.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.MatchResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class TemplateAutomatonClassFinder extends TemplateAbstractFinder {

    private static final String REGEX_TEMPLATE = "\\{\\{[^}]+}}";
    private final static List<RunAutomaton> AUTOMATA = new ArrayList<>();

    TemplateAutomatonClassFinder(List<String> words) {
        for (String word : words) {
            if (startsWithLowerCase(word)) {
                AUTOMATA.add(new RunAutomaton(new RegExp(String.format("\\{\\{ *%s *[|:](%s|[^}])+?}}", setFirstUpperCaseClass(word), REGEX_TEMPLATE)).toAutomaton()));
            } else {
                AUTOMATA.add(new RunAutomaton(new RegExp(String.format("\\{\\{ *%s *[|:](%s|[^}])+?}}", word, REGEX_TEMPLATE)).toAutomaton()));
            }
        }
    }

    Set<MatchResult> findMatches(String text) {
        Set<MatchResult> matches = new HashSet<>();
        for (RunAutomaton automaton : AUTOMATA) {
            AutomatonMatcher m = automaton.newMatcher(text);
            while (m.find()) {
                matches.add(new MatchResult(m.start(), m.group()));
            }
        }
        return matches;
    }

}
