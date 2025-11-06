package es.bvalero.replacer.finder.benchmark.iterable;

import es.bvalero.replacer.finder.FinderPage;

@FunctionalInterface
interface MutableMatchFinder {
    void findResult(FinderPage page, int start, MutableMatch match);
}
