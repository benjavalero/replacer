package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder2.Immutable;
import es.bvalero.replacer.finder2.ImmutableFinder;
import java.util.Iterator;
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
    public Iterator<Immutable> find(String text) {
        return find(text, AUTOMATON_CURSIVE, this::convertMatch);
    }

    private Immutable convertMatch(MatchResult match) {
        String text = match.group();
        int end = text.endsWith("\n") ? text.length() : text.length() - 1;
        return Immutable.of(match.start() + 1, text.substring(1, end));
    }
}
