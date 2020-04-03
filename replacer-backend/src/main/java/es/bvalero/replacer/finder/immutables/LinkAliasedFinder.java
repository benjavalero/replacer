package es.bvalero.replacer.finder.immutables;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.ImmutableFinderPriority;
import es.bvalero.replacer.finder.RegexIterable;
import java.util.regex.MatchResult;
import org.springframework.stereotype.Component;

/**
 * Find the first part of aliased links, e. g. `brasil` in `[[brasil|Brasil]]`
 */
@Component
public class LinkAliasedFinder implements ImmutableFinder {
    private static final String REGEX_LINK_ALIASED = "\\[\\[[^]|:\n]+\\|";
    private static final RunAutomaton AUTOMATON_LINK_ALIASED = new RunAutomaton(
        new RegExp(REGEX_LINK_ALIASED).toAutomaton()
    );

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.HIGH;
    }

    @Override
    public int getMaxLength() {
        return 50;
    }

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<>(text, AUTOMATON_LINK_ALIASED, this::convert);
    }

    @Override
    public Immutable convert(MatchResult match) {
        String text = match.group();
        String aliased = text.substring(2, text.length() - 1).trim();
        int pos = text.indexOf(aliased);
        return Immutable.of(match.start() + pos, aliased, this);
    }
}
