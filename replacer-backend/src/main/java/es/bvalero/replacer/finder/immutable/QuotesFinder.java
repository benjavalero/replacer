package es.bvalero.replacer.finder.immutable;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.RegexIterable;
import org.springframework.stereotype.Component;

/**
 * Find text in double quotes, e. g. `"text"`
 */
@Component
public class QuotesFinder implements ImmutableFinder {
    // For the automaton the quote needs an extra backslash
    private static final String REGEX_DOUBLE_QUOTES = "\\\"[^\\\"]+\\\"";
    private static final RunAutomaton AUTOMATON_DOUBLE_QUOTES = new RunAutomaton(
        new RegExp(REGEX_DOUBLE_QUOTES).toAutomaton()
    );

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<>(text, AUTOMATON_DOUBLE_QUOTES, this::convert);
    }
}
