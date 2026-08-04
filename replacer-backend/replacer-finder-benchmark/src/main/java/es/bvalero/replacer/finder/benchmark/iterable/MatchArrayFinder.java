package es.bvalero.replacer.finder.benchmark.iterable;

import es.bvalero.replacer.finder.FinderPage;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
interface MatchArrayFinder {
    @Nullable
    int[] findResult(FinderPage page, int start);
}
