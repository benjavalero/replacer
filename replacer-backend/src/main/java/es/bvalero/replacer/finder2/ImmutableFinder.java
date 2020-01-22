package es.bvalero.replacer.finder2;

import dk.brics.automaton.RunAutomaton;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Interface to be implemented by any class returning a collection of immutables.
 *
 * For performance reasons, it is preferred to return them as an interator.
 */
public interface ImmutableFinder extends RegexFinder<Immutable> {
    static final Function<MatchResult, Immutable> DEFAULT_TRANSFORM = matcher ->
        Immutable.of(matcher.start(), matcher.group());

    Iterator<Immutable> find(String text);

    default List<Immutable> findList(String text) {
        Iterable<Immutable> iterable = () -> find(text);
        return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
    }

    default Iterator<Immutable> find(String text, RunAutomaton automaton) {
        return find(text, automaton, DEFAULT_TRANSFORM);
    }
}
