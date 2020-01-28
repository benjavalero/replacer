package es.bvalero.replacer.finder;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RunAutomaton;
import java.util.Iterator;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Returns an interable of results after running a regex.
 * It has two constructors in order to use it with a pattern or an automaton.
 * It needs a function parameter to convert the match result into the desired type T.
 * We can also provide an optional predicate to validate the result against the text.
 */
public class RegexIterable<T> implements Iterable<T> {
    private final Pattern pattern;
    private final RunAutomaton automaton;
    private final String text;
    private final Function<MatchResult, T> convert;
    private final BiPredicate<MatchResult, String> isValid;

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
            return new RegexAutomatonIterator<>(text, automaton, convert, isValid);
        } else if (pattern != null) {
            return new RegexPatternIterator<>(text, pattern, convert, isValid);
        } else {
            throw new IllegalStateException();
        }
    }

    class RegexPatternIterator<R> implements Iterator<R> {
        private final String text;
        private final Matcher matcher;
        private final Function<MatchResult, R> convert;
        private final BiPredicate<MatchResult, String> isValid;

        RegexPatternIterator(
            String text,
            Pattern pattern,
            Function<MatchResult, R> convert,
            BiPredicate<MatchResult, String> isValid
        ) {
            this.text = text;
            this.matcher = pattern.matcher(text);
            this.convert = convert;
            this.isValid = isValid;
        }

        @Override
        public boolean hasNext() {
            boolean hasNext = false;
            do {
                hasNext = matcher.find();
            } while (hasNext && !isValidMatch(matcher, text));
            return hasNext;
        }

        private boolean isValidMatch(MatchResult match, String text) {
            if (isValid == null) {
                return true;
            } else {
                return isValid.test(match, text);
            }
        }

        @Override
        public R next() {
            return convert.apply(matcher);
        }
    }

    class RegexAutomatonIterator<R> implements Iterator<R> {
        private final String text;
        private final AutomatonMatcher matcher;
        private final Function<MatchResult, R> convert;
        private final BiPredicate<MatchResult, String> isValid;

        RegexAutomatonIterator(
            String text,
            RunAutomaton automaton,
            Function<MatchResult, R> convert,
            BiPredicate<MatchResult, String> isValid
        ) {
            this.text = text;
            this.matcher = automaton.newMatcher(text);
            this.convert = convert;
            this.isValid = isValid;
        }

        @Override
        public boolean hasNext() {
            boolean hasNext = false;
            do {
                hasNext = matcher.find();
            } while (hasNext && !isValidMatch(matcher, text));
            return hasNext;
        }

        private boolean isValidMatch(MatchResult match, String text) {
            if (isValid == null) {
                return true;
            } else {
                return isValid.test(match, text);
            }
        }

        @Override
        public R next() {
            return convert.apply(matcher);
        }
    }
}
