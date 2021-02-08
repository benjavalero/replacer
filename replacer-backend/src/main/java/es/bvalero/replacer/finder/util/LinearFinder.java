package es.bvalero.replacer.finder.util;

import es.bvalero.replacer.page.IndexablePage;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;

@FunctionalInterface
public interface LinearFinder {
    /**
     * @return the next result from the start position.
     */
    @Nullable
    MatchResult findResult(IndexablePage page, int start);
}
