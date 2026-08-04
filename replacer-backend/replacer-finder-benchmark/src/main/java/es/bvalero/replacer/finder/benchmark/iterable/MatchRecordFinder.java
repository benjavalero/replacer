package es.bvalero.replacer.finder.benchmark.iterable;

import es.bvalero.replacer.finder.FinderPage;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
interface MatchRecordFinder {
    @Nullable
    MatchRecord findResult(FinderPage page, int start);
}
