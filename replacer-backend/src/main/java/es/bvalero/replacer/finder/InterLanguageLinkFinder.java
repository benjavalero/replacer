package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

import org.springframework.stereotype.Component;

/**
 * Find inter-language links, e. g. `[[:pt:Title]]`
 */
@Component
class InterLanguageLinkFinder implements ImmutableFinder {
    private static final String REGEX_INTER_LANGUAGE_LINK = "\\[\\[:?[a-z]{2}:[^]]+]]";
    private static final RunAutomaton AUTOMATON_INTER_LANGUAGE_LINK = new RunAutomaton(
        new RegExp(REGEX_INTER_LANGUAGE_LINK).toAutomaton()
    );

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<Immutable>(text, AUTOMATON_INTER_LANGUAGE_LINK, this::convert, this::isValid);
    }
}
