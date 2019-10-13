package es.bvalero.replacer.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.IgnoredReplacement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class CompleteTagAutomatonNegatedFinder extends CompleteTagAbstractFinder {

    private static final List<RunAutomaton> AUTOMATA = new ArrayList<>();

    CompleteTagAutomatonNegatedFinder(List<String> words) {
        words.forEach(word -> AUTOMATA.add(new RunAutomaton(new RegExp(String.format("\\<%s[^\\>]*\\>.+\\</%s\\>", word, word)).toAutomaton())));
    }

    Set<IgnoredReplacement> findMatches(String text) {
        Set<IgnoredReplacement> matches = new HashSet<>();
        for (RunAutomaton automaton : AUTOMATA) {
            AutomatonMatcher m = automaton.newMatcher(text);
            while (m.find()) {
                matches.add(IgnoredReplacement.of(m.start(), m.group()));
            }
        }
        return matches;
    }

}
