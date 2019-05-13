package es.bvalero.replacer.finder;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RunAutomaton;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class to provide generic methods to find replacements
 */
public abstract class ReplacementFinder {

    public List<MatchResult> findMatchResults(CharSequence text, RunAutomaton automaton) {
        List<MatchResult> matches = new ArrayList<>(100);
        AutomatonMatcher matcher = automaton.newMatcher(text);
        while (matcher.find()) {
            matches.add(new MatchResult(matcher.start(), matcher.group(0)));
        }
        return matches;
    }

    /**
     * @return If the first letter of the word is uppercase
     */
    protected boolean startsWithUpperCase(CharSequence word) {
        return Character.isUpperCase(word.charAt(0));
    }

}
