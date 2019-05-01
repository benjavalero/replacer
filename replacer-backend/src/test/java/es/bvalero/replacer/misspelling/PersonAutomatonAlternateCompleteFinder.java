package es.bvalero.replacer.misspelling;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PersonAutomatonAlternateCompleteFinder extends WordFinder {

    private RunAutomaton words;

    PersonAutomatonAlternateCompleteFinder(Collection<String> words) {
        String alternations = "(" + StringUtils.join(words, "|") + ").<Lu>";
        this.words = new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
    }

    Set<WordMatch> findWords(String text) {
        Set<WordMatch> matches = new HashSet<>();
        AutomatonMatcher m = this.words.newMatcher(text);
        while (m.find()) {
            matches.add(new WordMatch(m.start(), m.group().substring(0, m.group().length() - 2)));
        }
        return matches;
    }

}
