package es.bvalero.replacer.finder.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

import java.util.*;

class PersonAutomatonCompleteFinder extends PersonAbstractFinder {

    private List<RunAutomaton> words;

    PersonAutomatonCompleteFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(new RunAutomaton(new RegExp(word + ".<Lu>").toAutomaton(new DatatypesAutomatonProvider())));
        }
    }

    Set<IgnoredReplacement> findMatches(String text) {
        // We loop over all the words and find them completely in the text with an automaton
        Set<IgnoredReplacement> matches = new HashSet<>();
        for (RunAutomaton word : this.words) {
            AutomatonMatcher m = word.newMatcher(text);
            while (m.find()) {
                matches.add(IgnoredReplacement.of(m.start(), m.group().substring(0, m.group().length() - 2)));
            }
        }
        return matches;
    }

}
