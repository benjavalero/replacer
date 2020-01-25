package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder2.Immutable;
import es.bvalero.replacer.finder2.ImmutableFinder;
import es.bvalero.replacer.finder2.RegexIterable;
import java.util.regex.MatchResult;
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
        return new RegexIterable<Immutable>(text, AUTOMATON_LINK_ALIASED, this::convertMatch, this::isValid);
    }

    private Immutable convertMatch(MatchResult match) {
        return Immutable.of(match.start() + 2, match.group().substring(2, match.group().length() - 1));
    }
}
