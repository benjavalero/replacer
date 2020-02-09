package es.bvalero.replacer.finder.immutable;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import java.util.regex.MatchResult;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.RegexIterable;
import org.springframework.stereotype.Component;

/**
 * Find the first part of aliased links, e. g. `brasil` in `[[brasil|Brasil]]`
 */
@Component
class LinkAliasedFinder implements ImmutableFinder {
    private static final String REGEX_LINK_ALIASED = "\\[\\[[^]|]+\\|";
    private static final RunAutomaton AUTOMATON_LINK_ALIASED = new RunAutomaton(
        new RegExp(REGEX_LINK_ALIASED).toAutomaton()
    );

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<>(text, AUTOMATON_LINK_ALIASED, this::convert);
    }

    @Override
    public Immutable convert(MatchResult match) {
        return Immutable.of(match.start() + 2, match.group().substring(2, match.group().length() - 1));
    }
}
