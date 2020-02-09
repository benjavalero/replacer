package es.bvalero.replacer.finder.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class PersonAutomatonAllFinder extends PersonAbstractFinder {

    private RunAutomaton wordPattern;
    private Set<String> words;

    PersonAutomatonAllFinder(Collection<String> words) {
        this.wordPattern = new RunAutomaton(new RegExp("<L>+").toAutomaton(new DatatypesAutomatonProvider()));
        this.words = new HashSet<>(words);
    }

    Set<FinderResult> findMatches(String text) {
        // Find all words in the text with an automaton and check if they are in the list
        Set<FinderResult> matches = new HashSet<>();
        AutomatonMatcher m = this.wordPattern.newMatcher(text);
        while (m.find()) {
            if (this.words.contains(m.group()) && isWordFollowedByUppercase(m.start(), m.group(), text)) {
                matches.add(FinderResult.of(m.start(), m.group()));
            }
        }
        return matches;
    }

}
