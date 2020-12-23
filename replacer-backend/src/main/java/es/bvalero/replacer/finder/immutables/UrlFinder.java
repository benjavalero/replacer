package es.bvalero.replacer.finder.immutables;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.ImmutableFinderPriority;
import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.page.IndexablePage;
import org.springframework.stereotype.Component;

/**
 * Find URLs, e.g. `https://www.google.es`
 */
@Component
public class UrlFinder implements ImmutableFinder {

    // The automaton works quite well and provides support for complex URLs so it is worth
    // to use it even if it doesn't give the best performance
    private static final String REGEX_URL = "https?://<URI>";

    private static final RunAutomaton AUTOMATON_URL = new RunAutomaton(
        new RegExp(REGEX_URL).toAutomaton(new DatatypesAutomatonProvider())
    );

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.MEDIUM;
    }

    @Override
    public Iterable<Immutable> find(IndexablePage page) {
        return new RegexIterable<>(page, AUTOMATON_URL, this::convert);
    }
}
