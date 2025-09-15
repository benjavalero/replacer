package es.bvalero.replacer.finder.benchmark.iterable;

import es.bvalero.replacer.finder.FinderPage;
import java.util.Iterator;
import java.util.NoSuchElementException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
class IterableMatchRangeFinder {

    public static Iterable<MatchRange> find(FinderPage page, MatchRangeFinder finder) {
        return () -> new LinearIterator(page, finder);
    }

    private static class LinearIterator implements Iterator<MatchRange> {

        private final FinderPage page;
        private final MatchRangeFinder finder;
        private int start;
        private MatchRange next;

        LinearIterator(FinderPage page, MatchRangeFinder finder) {
            this.page = page;
            this.finder = finder;
            this.start = 0;
            this.next = null;
        }

        @Override
        public boolean hasNext() {
            // This may throw an exception, but it is eventually captured by the indexer.
            MatchRange result = this.finder.findResult(this.page, this.start);
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
        public MatchRange next() {
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            return this.next;
        }
    }
}
