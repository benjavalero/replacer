package es.bvalero.replacer.finder;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.page.IndexablePage;
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
    private final IndexablePage page;
    private final Function<MatchResult, T> convert;
    private final BiPredicate<MatchResult, IndexablePage> isValid;

    public RegexIterable(IndexablePage page, Pattern pattern, Function<MatchResult, T> convert) {
        this.pattern = pattern;
        this.automaton = null;
        this.page = page;
        this.convert = convert;
        this.isValid = null;
    }

    public RegexIterable(
        IndexablePage page,
        Pattern pattern,
        Function<MatchResult, T> convert,
        BiPredicate<MatchResult, IndexablePage> isValid
    ) {
        this.pattern = pattern;
        this.automaton = null;
        this.page = page;
        this.convert = convert;
        this.isValid = isValid;
    }

    public RegexIterable(IndexablePage page, RunAutomaton automaton, Function<MatchResult, T> convert) {
        this.pattern = null;
        this.automaton = automaton;
        this.page = page;
        this.convert = convert;
        this.isValid = null;
    }

    public RegexIterable(
        IndexablePage page,
        RunAutomaton automaton,
        Function<MatchResult, T> convert,
        BiPredicate<MatchResult, IndexablePage> isValid
    ) {
        this.pattern = null;
        this.automaton = automaton;
        this.page = page;
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
            this.matcher = pattern.matcher(page.getContent());
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
                return isValid.test(match, page);
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
            this.matcher = automaton.newMatcher(page.getContent());
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
                return isValid.test(match, page);
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
