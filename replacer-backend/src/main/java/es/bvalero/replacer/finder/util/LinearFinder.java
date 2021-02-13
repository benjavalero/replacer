package es.bvalero.replacer.finder.util;

import es.bvalero.replacer.finder.common.FinderPage;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;

@FunctionalInterface
public interface LinearFinder {
    /**
     * @return the next result from the start position.
     */
    @Nullable
    MatchResult findResult(FinderPage page, int start);
}
