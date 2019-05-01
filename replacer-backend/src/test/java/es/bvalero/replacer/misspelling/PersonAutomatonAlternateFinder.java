package es.bvalero.replacer.misspelling;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class PersonAutomatonAlternateFinder extends WordFinder {

    private RunAutomaton words;

    PersonAutomatonAlternateFinder(Collection<String> words) {
        String alternations = '(' + StringUtils.join(words, "|") + ')';
        this.words = new RunAutomaton(new RegExp(alternations).toAutomaton());
    }

    Set<WordMatch> findWords(String text) {
        Set<WordMatch> matches = new HashSet<>();
        AutomatonMatcher m = this.words.newMatcher(text);
        while (m.find()) {
            WordMatch match = new WordMatch(m.start(), m.group());
            if (isWordFollowedByUppercase(match, text)) {
                matches.add(match);
            }
        }
        return matches;
    }

}
