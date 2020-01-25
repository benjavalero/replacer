package es.bvalero.replacer.finder2;

import java.util.Iterator;

/**
 * Helper class to iterate over an iterable of iterables, e. g. a list of lists.
 */
public class IterableOfIterable<T> implements Iterable<T> {
    private final Iterable<Iterable<T>> iterables;

    public IterableOfIterable(Iterable<Iterable<T>> iterables) {
        this.iterables = iterables;
    }

    @Override
    public Iterator<T> iterator() {
        return new IteratorOfIterable<T>(iterables);
    }

    class IteratorOfIterable<R> implements Iterator<R> {
        private final Iterator<Iterable<R>> iteratorOfIterators;
        private Iterator<R> currentIterator;

        IteratorOfIterable(Iterable<Iterable<R>> iterables) {
            this.iteratorOfIterators = iterables.iterator();
        }

        @Override
        public boolean hasNext() {
            while (currentIterator == null || !currentIterator.hasNext()) {
                if (!iteratorOfIterators.hasNext()) {
                    return false;
                }
                currentIterator = iteratorOfIterators.next().iterator();
            }
            return true;
        }

        @Override
        public R next() {
            return currentIterator.next();
        }
    }
}
