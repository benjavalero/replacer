package es.bvalero.replacer.finder;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.MatchResult;
import org.jetbrains.annotations.NotNull;

public class LinearIterable<T> implements Iterable<T> {
    private final String text;
    private final BiFunction<String, Integer, MatchResult> find;
    private final Function<MatchResult, T> convert;

    public LinearIterable(
        String text,
        BiFunction<String, Integer, MatchResult> find,
        Function<MatchResult, T> convert
    ) {
        this.text = text;
        this.find = find;
        this.convert = convert;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new LinearIterator<>(convert);
    }

    private class LinearIterator<R> implements Iterator<R> {
        private final Function<MatchResult, R> convert;
        private int start;
        private R next;

        LinearIterator(Function<MatchResult, R> convert) {
            this.convert = convert;
            this.start = 0;
            this.next = null;
        }

        @Override
        public boolean hasNext() {
            if (start >= text.length()) {
                return false;
            }
            MatchResult result = find.apply(text, start);
            if (result == null) {
                next = null;
                start = Integer.MAX_VALUE;
                return false;
            } else {
                next = convert.apply(result);
                start = result.end();
                return true;
            }
        }

        @Override
        public R next() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            return next;
        }
    }
}
