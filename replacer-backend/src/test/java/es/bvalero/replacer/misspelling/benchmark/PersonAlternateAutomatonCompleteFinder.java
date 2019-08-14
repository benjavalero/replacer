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

class PersonAlternateAutomatonCompleteFinder extends PersonAbstractFinder {

    private RunAutomaton words;

    PersonAlternateAutomatonCompleteFinder(Collection<String> words) {
        String alternations = "(" + StringUtils.join(words, "|") + ").<Lu>";
        this.words = new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
    }

    Set<MatchResult> findMatches(String text) {
        // Build an alternate automaton with all the complete words and match it against the text
        Set<MatchResult> matches = new HashSet<>();
        AutomatonMatcher m = this.words.newMatcher(text);
        while (m.find()) {
            matches.add(MatchResult.of(m.start(), m.group().substring(0, m.group().length() - 2)));
        }
        return matches;
    }

}
