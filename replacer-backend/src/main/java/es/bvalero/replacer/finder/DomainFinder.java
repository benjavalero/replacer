package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class DomainFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_DOMAIN = "[A-Za-z.]+\\.[a-z]{2,4}[^A-Za-z]";
    private static final RunAutomaton AUTOMATON_DOMAIN = new RunAutomaton(new RegExp(REGEX_DOMAIN).toAutomaton());

    @Override
    public List<IgnoredReplacement> findIgnoredReplacements(String text) {
        return findMatchResults(text, AUTOMATON_DOMAIN);
    }

    @Override
    public IgnoredReplacement convertMatch(int start, String text) {
        // Remove the last character
        return IgnoredReplacement.of(start, text.substring(0, text.length() - 1));
    }

}
