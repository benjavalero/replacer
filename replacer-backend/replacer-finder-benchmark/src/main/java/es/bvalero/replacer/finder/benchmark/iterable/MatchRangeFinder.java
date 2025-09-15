package es.bvalero.replacer.finder.benchmark.iterable;

import es.bvalero.replacer.finder.FinderPage;
import org.springframework.lang.Nullable;

@FunctionalInterface
interface MatchRangeFinder {
    @Nullable
    MatchRange findResult(FinderPage page, int start);
}
