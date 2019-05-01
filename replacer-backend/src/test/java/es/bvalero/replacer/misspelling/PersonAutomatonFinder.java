package es.bvalero.replacer.misspelling;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

import java.util.*;

class PersonAutomatonFinder extends WordFinder {

    private List<RunAutomaton> words;

    PersonAutomatonFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(new RunAutomaton(new RegExp(word).toAutomaton()));
        }
    }

    Set<WordMatch> findWords(String text) {
        Set<WordMatch> matches = new HashSet<>();
        for (RunAutomaton word : this.words) {
            AutomatonMatcher m = word.newMatcher(text);
            while (m.find()) {
                WordMatch match = new WordMatch(m.start(), m.group());
                if (isWordFollowedByUppercase(match, text)) {
                    matches.add(match);
                }
            }
        }
        return matches;
    }

}
