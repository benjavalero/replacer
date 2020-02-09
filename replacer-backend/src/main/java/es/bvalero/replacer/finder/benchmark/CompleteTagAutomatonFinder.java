package es.bvalero.replacer.finder.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class CompleteTagAutomatonFinder extends CompleteTagAbstractFinder {

    private static final List<RunAutomaton> AUTOMATA = new ArrayList<>();

    CompleteTagAutomatonFinder(List<String> words) {
        words.forEach(word -> AUTOMATA.add(new RunAutomaton(new RegExp(String.format("\\<%s.*\\>.+\\</%s\\>", word, word)).toAutomaton())));
    }

    Set<FinderResult> findMatches(String text) {
        Set<FinderResult> matches = new HashSet<>();
        for (RunAutomaton automaton : AUTOMATA) {
            AutomatonMatcher m = automaton.newMatcher(text);
            while (m.find()) {
                matches.add(FinderResult.of(m.start(), m.group()));
            }
        }
        return matches;
    }

}
