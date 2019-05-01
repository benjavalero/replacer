package es.bvalero.replacer.misspelling;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

import java.util.*;

class PersonAutomatonCompleteFinder extends WordFinder {

    private List<RunAutomaton> words;

    PersonAutomatonCompleteFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(new RunAutomaton(new RegExp(word + ".<Lu>").toAutomaton(new DatatypesAutomatonProvider())));
        }
    }

    Set<WordMatch> findWords(String text) {
        Set<WordMatch> matches = new HashSet<>();
        for (RunAutomaton word : this.words) {
            AutomatonMatcher m = word.newMatcher(text);
            while (m.find()) {
                matches.add(new WordMatch(m.start(), m.group().substring(0, m.group().length() - 2)));
            }
        }
        return matches;
    }

}
