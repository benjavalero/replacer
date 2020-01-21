package es.bvalero.replacer.finder2;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RunAutomaton;
import java.util.Iterator;
import java.util.regex.MatchResult;

/**
 * Helper abstract class to find immutables using a regex automaton.
 */
public abstract class ImmutableAutomatonFinder implements ImmutableFinder {

    public abstract RunAutomaton getAutomaton();

    public Immutable getImmutableFromResult(MatchResult matcher) {
        return Immutable.of(matcher.start(), matcher.group());
    }

    @Override
    public Iterator<Immutable> findImmutables(String text) {
        AutomatonMatcher matcher = getAutomaton().newMatcher(text);
        return new Iterator<Immutable>() {

            @Override
            public boolean hasNext() {
                return matcher.find();
            }

            @Override
            public Immutable next() {
                return getImmutableFromResult(matcher);
            }
        };
    }
}
