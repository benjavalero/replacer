package es.bvalero.replacer.finder;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder2.ImmutableAutomatonFinder;
import es.bvalero.replacer.finder2.ImmutableFinder;
import org.springframework.stereotype.Component;

/**
 * Find immutables of type URL, e. g. https://www.google.es
 */
@Component
class UrlFinder extends ImmutableAutomatonFinder implements ImmutableFinder {
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_URL = "https?://<URI>";

    private static final RunAutomaton AUTOMATON_URL = new RunAutomaton(
        new RegExp(REGEX_URL).toAutomaton(new DatatypesAutomatonProvider())
    );

    @Override
    public RunAutomaton getAutomaton() {
        return AUTOMATON_URL;
    }
}
