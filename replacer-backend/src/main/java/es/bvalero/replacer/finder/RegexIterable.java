package es.bvalero.replacer.finder;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RunAutomaton;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Returns an iterable of results after running a regex.
 * It has two constructors in order to use it with a pattern or an automaton.
 * It needs a function parameter to convert the match result into the desired type T.
 * We overload the constructors with an optional predicate to validate the result against the text.
 */
public class RegexIterable<T> implements Iterable<T> {
    private final Pattern pattern;
    private final RunAutomaton automaton;
    private final String text;
    private final Function<MatchResult, T> convert;
    private final BiPredicate<MatchResult, String> isValid;

    public RegexIterable(String text, Pattern pattern, Function<MatchResult, T> convert) {
        this.pattern = pattern;
        this.automaton = null;
        this.text = text;
        this.convert = convert;
        this.isValid = null;
    }

    public RegexIterable(
        String text,
        Pattern pattern,
        Function<MatchResult, T> convert,
        BiPredicate<MatchResult, String> isValid
    ) {
        this.pattern = pattern;
        this.automaton = null;
        this.text = text;
        this.convert = convert;
        this.isValid = isValid;
    }

    public RegexIterable(String text, RunAutomaton automaton, Function<MatchResult, T> convert) {
        this.pattern = null;
        this.automaton = automaton;
        this.text = text;
        this.convert = convert;
        this.isValid = null;
    }

    public RegexIterable(
        String text,
        RunAutomaton automaton,
        Function<MatchResult, T> convert,
        BiPredicate<MatchResult, String> isValid
    ) {
        this.pattern = null;
        this.automaton = automaton;
        this.text = text;
        this.convert = convert;
        this.isValid = isValid;
    }

    @Override
    public Iterator<T> iterator() {
        if (automaton != null) {
            return new RegexAutomatonIterator<>(convert);
        } else if (pattern != null) {
            return new RegexPatternIterator<>(convert);
        } else {
            throw new IllegalStateException();
        }
    }

    private class RegexPatternIterator<R> implements Iterator<R> {
        private final Matcher matcher;
        private final Function<MatchResult, R> convert;

        RegexPatternIterator(Function<MatchResult, R> convert) {
            this.matcher = pattern.matcher(text);
            this.convert = convert;
        }

        @Override
        public boolean hasNext() {
            boolean hasNext;
            do {
                hasNext = matcher.find();
            } while (hasNext && !isValidMatch(matcher));
            return hasNext;
        }

        private boolean isValidMatch(MatchResult match) {
            if (isValid == null) {
                return true;
            } else {
                return isValid.test(match, text);
            }
        }

        @Override
        public R next() {
            if (matcher == null || !isValidMatch(matcher)) {
                throw new NoSuchElementException();
            }
            return convert.apply(matcher);
        }
    }

    private class RegexAutomatonIterator<R> implements Iterator<R> {
        private final AutomatonMatcher matcher;
        private final Function<MatchResult, R> convert;

        RegexAutomatonIterator(Function<MatchResult, R> convert) {
            this.matcher = automaton.newMatcher(text);
            this.convert = convert;
        }

        @Override
        public boolean hasNext() {
            boolean hasNext;
            do {
                hasNext = matcher.find();
            } while (hasNext && !isValidMatch(matcher));
            return hasNext;
        }

        private boolean isValidMatch(MatchResult match) {
            if (isValid == null) {
                return true;
            } else {
                return isValid.test(match, text);
            }
        }

        @Override
        public R next() {
            if (matcher == null || !isValidMatch(matcher)) {
                throw new NoSuchElementException();
            }
            return convert.apply(matcher);
        }
    }
}
