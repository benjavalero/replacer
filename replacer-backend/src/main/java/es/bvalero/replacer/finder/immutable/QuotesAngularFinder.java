package es.bvalero.replacer.finder.immutable;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.RegexIterable;
import org.springframework.stereotype.Component;

/**
 * Find text in angular quotes, e. g. `«text»`
 */
@Component
public class QuotesAngularFinder implements ImmutableFinder {
    private static final String REGEX_ANGULAR_QUOTES = "«[^»]+»";
    private static final RunAutomaton AUTOMATON_ANGULAR_QUOTES = new RunAutomaton(
        new RegExp(REGEX_ANGULAR_QUOTES).toAutomaton()
    );

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<>(text, AUTOMATON_ANGULAR_QUOTES, this::convert);
    }
}
