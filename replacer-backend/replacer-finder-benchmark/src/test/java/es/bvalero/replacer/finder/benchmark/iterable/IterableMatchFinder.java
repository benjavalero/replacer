package es.bvalero.replacer.finder.benchmark.iterable;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.LinearFinder;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.MatchResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IterableMatchFinder {

    public static Iterable<MatchResult> find(FinderPage page, LinearFinder finder) {
        return () -> new LinearIterator(page, finder);
    }

    private static class LinearIterator implements Iterator<MatchResult> {

        private final FinderPage page;
        private final LinearFinder finder;
        private int start;
        private MatchResult next;

        LinearIterator(FinderPage page, LinearFinder finder) {
            this.page = page;
            this.finder = finder;
            this.start = 0;
            this.next = null;
        }

        @Override
        public boolean hasNext() {
            // This may throw an exception, but it is eventually captured by the indexer.
            MatchResult result = this.finder.findResult(this.page, this.start);
            if (result == null) {
                this.next = null;
                this.start = Integer.MAX_VALUE;
                return false;
            } else {
                this.next = result;
                this.start = result.end();
                return true;
            }
        }

        @Override
        public MatchResult next() {
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            return this.next;
        }
    }
}
