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
 * Find text in cursive, e. g. `''cursive''` in `This is a ''cursive'' example`
 */
@Component
class CursiveFinder implements ImmutableFinder {
    // There are limitations in the automaton (need to capture more than 1 character in some places) but it is faster
    private static final String REGEX_TWO_QUOTES_ONLY = "[^']''[^']";
    private static final String REGEX_CURSIVE = "%s(('''''|'''|')?[^'\n])*(%s|\n)";
    private static final RunAutomaton AUTOMATON_CURSIVE = new RunAutomaton(
        new RegExp(String.format(REGEX_CURSIVE, REGEX_TWO_QUOTES_ONLY, REGEX_TWO_QUOTES_ONLY)).toAutomaton()
    );

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.MEDIUM;
    }

    @Override
    public int getMaxLength() {
        return 500;
    }

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<>(text, AUTOMATON_CURSIVE, this::convert);
    }

    @Override
    public Immutable convert(MatchResult match) {
        String text = match.group();
        int end = text.endsWith("\n") ? text.length() : text.length() - 1;
        return Immutable.of(match.start() + 1, text.substring(1, end), this);
    }
}
