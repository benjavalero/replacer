package es.bvalero.replacer.misspelling;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

import java.util.*;

class UppercaseAutomatonFinder extends WordFinder {

    private Map<String, RunAutomaton> words;

    UppercaseAutomatonFinder(Collection<String> words) {
        this.words = new HashMap<>();
        for (String word : words) {
            this.words.put(word, new RunAutomaton(new RegExp("[!#*|=.]<Z>*" + word).toAutomaton(new DatatypesAutomatonProvider())));
        }
    }

    Set<WordMatch> findWords(String text) {
        Set<WordMatch> matches = new HashSet<>();
        for (Map.Entry<String, RunAutomaton> word : this.words.entrySet()) {
            AutomatonMatcher m = word.getValue().newMatcher(text);
            while (m.find()) {
                int pos = m.group().indexOf(word.getKey());
                WordMatch match = new WordMatch(m.start() + pos, word.getKey());
                matches.add(match);
            }
        }
        return matches;
    }

}
