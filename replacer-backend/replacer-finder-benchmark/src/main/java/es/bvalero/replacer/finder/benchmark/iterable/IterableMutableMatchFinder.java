package es.bvalero.replacer.finder.benchmark.iterable;

import es.bvalero.replacer.finder.FinderPage;
import java.util.Iterator;
import java.util.NoSuchElementException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
class IterableMutableMatchFinder {

    public static Iterable<MutableMatch> find(FinderPage page, MutableMatchFinder finder, MutableMatch match) {
        return () -> new LinearIterator(page, finder, match);
    }

    private static class LinearIterator implements Iterator<MutableMatch> {

        private final FinderPage page;
        private final MutableMatchFinder finder;
        private int start;
        private final MutableMatch next;

        LinearIterator(FinderPage page, MutableMatchFinder finder, MutableMatch match) {
            this.page = page;
            this.finder = finder;
            this.start = 0;
            this.next = match;
        }

        @Override
        public boolean hasNext() {
            // This may throw an exception, but it is eventually captured by the indexer.
            this.finder.findResult(this.page, this.start, this.next);
            if (this.next.getStart() < 0) {
                this.start = Integer.MAX_VALUE;
                return false;
            } else {
                this.start = this.next.end();
                return true;
            }
        }

        @Override
        public MutableMatch next() {
            if (this.next.getStart() < 0) {
                throw new NoSuchElementException();
            }
            return this.next;
        }
    }
}
