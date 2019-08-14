package es.bvalero.replacer.misspelling.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.MatchResult;

import java.util.*;

class UppercaseAutomatonFinder extends UppercaseAbstractFinder {

    private Map<String, RunAutomaton> words;

    UppercaseAutomatonFinder(Collection<String> words) {
        this.words = new HashMap<>();
        for (String word : words) {
            this.words.put(word, new RunAutomaton(new RegExp("[!#*|=.]<Z>*" + word).toAutomaton(new DatatypesAutomatonProvider())));
        }
    }

    Set<MatchResult> findMatches(String text) {
        // We loop over all the words and find them in the text with an automaton
        Set<MatchResult> matches = new HashSet<>();
        for (Map.Entry<String, RunAutomaton> word : this.words.entrySet()) {
            AutomatonMatcher m = word.getValue().newMatcher(text);
            while (m.find()) {
                int pos = m.group().indexOf(word.getKey());
                matches.add(MatchResult.of(m.start() + pos, word.getKey()));
            }
        }
        return matches;
    }

}
