package es.bvalero.replacer.finder.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

import java.util.*;

class PersonAutomatonFinder extends PersonAbstractFinder {

    private List<RunAutomaton> words;

    PersonAutomatonFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(new RunAutomaton(new RegExp(word).toAutomaton()));
        }
    }

    Set<FinderResult> findMatches(String text) {
        // We loop over all the words and find them in the text with an automaton
        Set<FinderResult> matches = new HashSet<>();
        for (RunAutomaton word : this.words) {
            AutomatonMatcher m = word.newMatcher(text);
            while (m.find()) {
                if (isWordFollowedByUppercase(m.start(), m.group(), text)) {
                    matches.add(FinderResult.of(m.start(), m.group()));
                }
            }
        }
        return matches;
    }

}
