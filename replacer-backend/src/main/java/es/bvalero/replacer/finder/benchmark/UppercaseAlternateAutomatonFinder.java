package es.bvalero.replacer.finder.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class UppercaseAlternateAutomatonFinder extends UppercaseAbstractFinder {

    private RunAutomaton words;

    UppercaseAlternateAutomatonFinder(Collection<String> words) {
        String alternations = "[!#*|=.]<Z>*(" + StringUtils.join(words, "|") + ')';
        this.words = new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
    }

    Set<FinderResult> findMatches(String text) {
        // Build an alternate automaton with all the words and match it against the text
        Set<FinderResult> matches = new HashSet<>();
        AutomatonMatcher m = this.words.newMatcher(text);
        while (m.find()) {
            String word = m.group().substring(1).trim();
            int pos = m.group().indexOf(word);
            matches.add(FinderResult.of(m.start() + pos, word));
        }
        return matches;
    }

}
