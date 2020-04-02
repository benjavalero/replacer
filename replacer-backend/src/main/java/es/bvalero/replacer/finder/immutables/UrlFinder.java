package es.bvalero.replacer.finder.immutables;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.ImmutableFinderPriority;
import es.bvalero.replacer.finder.RegexIterable;
import org.springframework.stereotype.Component;

/**
 * Find URLs, e. g. `https://www.google.es`
 */
@Component
public class UrlFinder implements ImmutableFinder {
    private static final String REGEX_URL = "https?://<URI>";

    private static final RunAutomaton AUTOMATON_URL = new RunAutomaton(
        new RegExp(REGEX_URL).toAutomaton(new DatatypesAutomatonProvider())
    );

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.MEDIUM;
    }

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<>(text, AUTOMATON_URL, this::convert);
    }
}
