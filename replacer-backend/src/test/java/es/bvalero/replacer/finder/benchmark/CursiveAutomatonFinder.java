package es.bvalero.replacer.finder.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.MatchResult;

import java.util.HashSet;
import java.util.Set;

class CursiveAutomatonFinder extends CursiveAbstractFinder {

    private static final String TWO_QUOTES_ONLY = "[^']''[^']";
    private static final String CURSIVE_REGEX = "%s(('''''|'''|')?[^'\n])*(%s|\n)";
    private static final RunAutomaton CURSIVE_AUTOMATON = new RunAutomaton(new RegExp(String.format(CURSIVE_REGEX, TWO_QUOTES_ONLY, TWO_QUOTES_ONLY)).toAutomaton());

    Set<MatchResult> findMatches(String text) {
        Set<MatchResult> matches = new HashSet<>();
        AutomatonMatcher m = CURSIVE_AUTOMATON.newMatcher(text);
        while (m.find()) {
            int start = m.start() + 1;
            int end = m.group().endsWith("\n") ? m.group().length() : m.group().length() - 1;
            String group = m.group().substring(1, end);
            matches.add(MatchResult.of(start, group));
        }
        return matches;
    }

}
