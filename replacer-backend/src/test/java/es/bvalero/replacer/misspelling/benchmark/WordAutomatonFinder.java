package es.bvalero.replacer.misspelling.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WordAutomatonFinder extends WordFinder {

    private List<RunAutomaton> words;

    WordAutomatonFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(new RunAutomaton(new RegExp(word).toAutomaton()));
        }
    }

    Set<WordMatch> findWords(String text) {
        // We loop over all the words and find them in the text with the indexOf function
        Set<WordMatch> matches = new HashSet<>();
        for (RunAutomaton word : this.words) {
            AutomatonMatcher m = word.newMatcher(text);
            while (m.find()) {
                WordMatch match = new WordMatch(m.start(), m.group());
                if (isWordCompleteInText(match, text)) {
                    matches.add(match);
                }
            }
        }
        return matches;
    }

}
