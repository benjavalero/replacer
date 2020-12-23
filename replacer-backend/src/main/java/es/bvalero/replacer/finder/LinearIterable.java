package es.bvalero.replacer.finder;

import es.bvalero.replacer.page.IndexablePage;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.MatchResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LinearIterable<T> implements Iterable<T> {

    private final IndexablePage page;
    private final BiFunction<IndexablePage, Integer, MatchResult> find;
    private final Function<MatchResult, T> convert;

    public LinearIterable(
        IndexablePage page,
        BiFunction<IndexablePage, Integer, MatchResult> find,
        Function<MatchResult, T> convert
    ) {
        this.page = page;
        this.find = find;
        this.convert = convert;
    }

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
            if (start >= page.getContent().length()) {
                return false;
            }
            MatchResult result = null;
            try {
                result = find.apply(page, start);
            } catch (Exception e) {
                LOGGER.error("Error finding match result: {} - {}", start, page.getContent(), e);
            }
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
