package es.bvalero.replacer.misspelling;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class UppercaseAutomatonAlternateFinder extends WordFinder {

    private RunAutomaton words;

    UppercaseAutomatonAlternateFinder(Collection<String> words) {
        String alternations = "[!#*|=.]<Z>*(" + StringUtils.join(words, "|") + ')';
        this.words = new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
    }

    Set<WordMatch> findWords(String text) {
        Set<WordMatch> matches = new HashSet<>();

        AutomatonMatcher m = this.words.newMatcher(text);
        while (m.find()) {
            String word = m.group().substring(1).trim();
            int pos = m.group().indexOf(word);
            WordMatch match = new WordMatch(m.start() + pos, word);
            matches.add(match);
        }

        return matches;
    }

}
