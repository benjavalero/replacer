package es.bvalero.replacer.finder;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class UrlFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_URL = "https?://<URI>";
    private static final RunAutomaton AUTOMATON_URL =
            new RunAutomaton(new RegExp(REGEX_URL).toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<IgnoredReplacement> findIgnoredReplacements(String text) {
        return findMatchResults(text, AUTOMATON_URL);
    }

}
