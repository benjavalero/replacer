package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class CursiveFinder implements IgnoredReplacementFinder {

    // There are limitations in the automaton (need to capture more than 1 character in some places) but it is faster
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_TWO_QUOTES_ONLY = "[^']''[^']";
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_CURSIVE = "%s(('''''|'''|')?[^'\n])*(%s|\n)";
    private static final RunAutomaton AUTOMATON_CURSIVE = new RunAutomaton(new RegExp(
            String.format(REGEX_CURSIVE, REGEX_TWO_QUOTES_ONLY, REGEX_TWO_QUOTES_ONLY)).toAutomaton());

    @Override
    public List<IgnoredReplacement> findIgnoredReplacements(String text) {
        return findMatchResults(text, AUTOMATON_CURSIVE);
    }

    @Override
    public IgnoredReplacement convertMatch(int start, String text) {
        int end = text.endsWith("\n") ? text.length() : text.length() - 1;
        return IgnoredReplacement.of(start + 1, text.substring(1, end));
    }

}
