package es.bvalero.replacer.finder.benchmark.domain;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import java.util.HashSet;
import java.util.Set;

class DomainAutomatonFinder extends DomainFinder {
    private static final String REGEX_DOMAIN = "[^A-Za-z./_-][A-Za-z.]+\\.[a-z]{2,4}[^a-z]";
    private static final RunAutomaton AUTOMATON_DOMAIN = new RunAutomaton(new RegExp(REGEX_DOMAIN).toAutomaton());

    Set<String> findMatches(String text) {
        Set<String> matches = new HashSet<>();
        AutomatonMatcher m = AUTOMATON_DOMAIN.newMatcher(text);
        while (m.find()) {
            matches.add(m.group().substring(1, m.group().length() - 1));
        }
        return matches;
    }
}
