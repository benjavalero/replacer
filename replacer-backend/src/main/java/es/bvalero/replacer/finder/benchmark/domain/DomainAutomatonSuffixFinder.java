package es.bvalero.replacer.finder.benchmark.domain;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

class DomainAutomatonSuffixFinder extends DomainFinder {
    private static final String REGEX_DOMAIN =
        String.format("[A-Za-z0-9.\\-]+\\.(%s)[^a-z]", StringUtils.join(SUFFIXES, "|"));
    private static final RunAutomaton AUTOMATON_DOMAIN = new RunAutomaton(new RegExp(REGEX_DOMAIN).toAutomaton());

    Set<String> findMatches(String text) {
        Set<String> matches = new HashSet<>();
        AutomatonMatcher m = AUTOMATON_DOMAIN.newMatcher(text);
        while (m.find()) {
            matches.add(m.group().substring(0, m.group().length() - 1));
        }
        return matches;
    }
}
