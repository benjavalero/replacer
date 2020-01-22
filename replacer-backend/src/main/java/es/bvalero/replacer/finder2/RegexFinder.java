package es.bvalero.replacer.finder2;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RunAutomaton;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Abstract class to find results from a regular expression.
 * For simplicity, it is exposed as an interface with default methods.
 */
interface RegexFinder<T> {
    default boolean isValidMatch(MatchResult match, String text) {
        return true;
    }

    default Iterator<T> find(String text, Pattern pattern, Function<MatchResult, T> transform) {
        Matcher matcher = pattern.matcher(text);
        return new Iterator<T>() {

            @Override
            public boolean hasNext() {
                boolean hasNext = false;
                do {
                    hasNext = matcher.find();
                } while (hasNext && !isValidMatch(matcher, text));
                return hasNext;
            }

            @Override
            public T next() {
                return transform.apply(matcher);
            }
        };
    }

    default Iterable<T> findIterable(String text, Pattern pattern, Function<MatchResult, T> transform) {
        return () -> find(text, pattern, transform);
    }

    default Stream<T> findStream(String text, Pattern pattern, Function<MatchResult, T> transform) {
        return StreamSupport.stream(findIterable(text, pattern, transform).spliterator(), false);
    }

    default List<T> findList(String text, Pattern pattern, Function<MatchResult, T> transform) {
        return findStream(text, pattern, transform).collect(Collectors.toList());
    }

    default Iterator<T> find(String text, RunAutomaton automaton, Function<MatchResult, T> transform) {
        AutomatonMatcher matcher = automaton.newMatcher(text);
        return new Iterator<T>() {

            @Override
            public boolean hasNext() {
                boolean hasNext = false;
                do {
                    hasNext = matcher.find();
                } while (hasNext && !isValidMatch(matcher, text));
                return hasNext;
            }

            @Override
            public T next() {
                return transform.apply(matcher);
            }
        };
    }

    default Iterable<T> findIterable(String text, RunAutomaton automaton, Function<MatchResult, T> transform) {
        return () -> find(text, automaton, transform);
    }

    default Stream<T> findStream(String text, RunAutomaton automaton, Function<MatchResult, T> transform) {
        return StreamSupport.stream(findIterable(text, automaton, transform).spliterator(), false);
    }

    default List<T> findList(String text, RunAutomaton automaton, Function<MatchResult, T> transform) {
        return findStream(text, automaton, transform).collect(Collectors.toList());
    }
}
