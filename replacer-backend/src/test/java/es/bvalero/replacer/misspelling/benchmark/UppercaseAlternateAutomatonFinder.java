package es.bvalero.replacer.misspelling.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.IgnoredReplacement;
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

    Set<IgnoredReplacement> findMatches(String text) {
        // Build an alternate automaton with all the words and match it against the text
        Set<IgnoredReplacement> matches = new HashSet<>();
        AutomatonMatcher m = this.words.newMatcher(text);
        while (m.find()) {
            String word = m.group().substring(1).trim();
            int pos = m.group().indexOf(word);
            matches.add(IgnoredReplacement.of(m.start() + pos, word));
        }
        return matches;
    }

}
