package es.bvalero.replacer.finder;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

import org.springframework.stereotype.Component;

/**
 * Find links with suffix, e. g. `[[brasil]]eño`
 */
@Component
class LinkSuffixedFinder implements ImmutableFinder {
    private static final String REGEX_LINK_SUFFIXED = "\\[\\[[^]]+]]<Ll>+";
    private static final RunAutomaton AUTOMATON_LINK_SUFFIXED = new RunAutomaton(
        new RegExp(REGEX_LINK_SUFFIXED).toAutomaton(new DatatypesAutomatonProvider())
    );

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<Immutable>(text, AUTOMATON_LINK_SUFFIXED, this::convert, this::isValid);
    }
}
