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
 * Find links with suffix, e. g. `[[brasil]]eño`
 */
@Component
public class LinkSuffixedFinder implements ImmutableFinder {
    private static final String REGEX_LINK_SUFFIXED = "\\[\\[[^]]+]]<Ll>+";
    private static final RunAutomaton AUTOMATON_LINK_SUFFIXED = new RunAutomaton(
        new RegExp(REGEX_LINK_SUFFIXED).toAutomaton(new DatatypesAutomatonProvider())
    );

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.LOW;
    }

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<>(text, AUTOMATON_LINK_SUFFIXED, this::convert);
    }
}
