package es.bvalero.replacer.misspelling.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.IgnoredReplacement;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class PersonAlternateAutomatonFinder extends PersonAbstractFinder {

    private RunAutomaton words;

    PersonAlternateAutomatonFinder(Collection<String> words) {
        String alternations = '(' + StringUtils.join(words, "|") + ')';
        this.words = new RunAutomaton(new RegExp(alternations).toAutomaton());
    }

    Set<IgnoredReplacement> findMatches(String text) {
        // Build an alternate automaton with all the words and match it against the text
        Set<IgnoredReplacement> matches = new HashSet<>();
        AutomatonMatcher m = this.words.newMatcher(text);
        while (m.find()) {
            if (isWordFollowedByUppercase(m.start(), m.group(), text)) {
                matches.add(IgnoredReplacement.of(m.start(), m.group()));
            }
        }
        return matches;
    }

}
