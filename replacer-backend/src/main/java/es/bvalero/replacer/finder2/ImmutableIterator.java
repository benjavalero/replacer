package es.bvalero.replacer.finder2;

import java.util.Iterator;
import java.util.List;

/**
 * An iterator of immutable finders. The purpose of this iterator is, given a list of immutable finders,
 * return all the immutables from the first finder, then all the immutables from the second one, and so on.
 * However, the immutables from any finder are also returned one by one.
 */
class ImmutableIterator implements Iterator<Immutable> {
    private final String text;
    private final Iterator<ImmutableFinder> finderIterator;
    private Iterator<Immutable> currentFinderResults;

    ImmutableIterator(String text, List<ImmutableFinder> finderList) {
        this.text = text;
        this.finderIterator = finderList.iterator();
    }

    @Override
    public boolean hasNext() {
        while (currentFinderResults == null || !currentFinderResults.hasNext()) {
            if (!finderIterator.hasNext()) {
                return false;
            }
            currentFinderResults = finderIterator.next().findImmutables(text);
        }
        return true;
    }

    @Override
    public Immutable next() {
        return currentFinderResults.next();
    }
}
