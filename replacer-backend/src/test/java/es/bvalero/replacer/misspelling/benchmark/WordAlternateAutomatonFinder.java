package es.bvalero.replacer.misspelling.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.MatchResult;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class WordAlternateAutomatonFinder extends WordAbstractFinder {

    private RunAutomaton words;

    WordAlternateAutomatonFinder(Collection<String> words) {
        String alternations = '(' + StringUtils.join(words, "|") + ')';
        this.words = new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
    }

    Set<MatchResult> findMatches(String text) {
        // Build an alternate automaton with all the words and match it against the text
        Set<MatchResult> matches = new HashSet<>();
        AutomatonMatcher m = this.words.newMatcher(text);
        while (m.find()) {
            if (isWordCompleteInText(m.start(), m.group(), text)) {
                matches.add(MatchResult.of(m.start(), m.group()));
            }
        }
        return matches;
    }

}
