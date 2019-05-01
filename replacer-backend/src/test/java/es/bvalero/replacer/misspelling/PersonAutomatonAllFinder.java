package es.bvalero.replacer.misspelling;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class PersonAutomatonAllFinder extends WordFinder {

    private RunAutomaton wordPattern;
    private Set<String> words;

    PersonAutomatonAllFinder(Collection<String> words) {
        this.wordPattern = new RunAutomaton(new RegExp("<L>+").toAutomaton(new DatatypesAutomatonProvider()));
        this.words = new HashSet<>(words);
    }

    Set<WordMatch> findWords(String text) {
        Set<WordMatch> matches = new HashSet<>();
        AutomatonMatcher m = this.wordPattern.newMatcher(text);
        while (m.find()) {
            WordMatch match = new WordMatch(m.start(), m.group());
            if (this.words.contains(match.getText()) && isWordFollowedByUppercase(match, text)) {
                matches.add(match);
            }
        }
        return matches;
    }

}
