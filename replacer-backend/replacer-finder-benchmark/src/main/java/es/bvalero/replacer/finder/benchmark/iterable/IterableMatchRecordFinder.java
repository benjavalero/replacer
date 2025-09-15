package es.bvalero.replacer.finder.benchmark.iterable;

import es.bvalero.replacer.finder.FinderPage;
import java.util.Iterator;
import java.util.NoSuchElementException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
class IterableMatchRecordFinder {

    public static Iterable<MatchRecord> find(FinderPage page, MatchRecordFinder finder) {
        return () -> new LinearIterator(page, finder);
    }

    private static class LinearIterator implements Iterator<MatchRecord> {

        private final FinderPage page;
        private final MatchRecordFinder finder;
        private int start;
        private MatchRecord next;

        LinearIterator(FinderPage page, MatchRecordFinder finder) {
            this.page = page;
            this.finder = finder;
            this.start = 0;
            this.next = null;
        }

        @Override
        public boolean hasNext() {
            // This may throw an exception, but it is eventually captured by the indexer.
            MatchRecord result = this.finder.findResult(this.page, this.start);
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
        public MatchRecord next() {
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            return this.next;
        }
    }
}
