package es.bvalero.replacer.finder.util;

import es.bvalero.replacer.common.domain.WikipediaPage;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;

@FunctionalInterface
public interface LinearFinder {
    /**
     * @return the next result from the start position.
     */
    @Nullable
    default MatchResult findResult(WikipediaPage page, int start) {
        final List<MatchResult> matches = new ArrayList<>();
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findResult(page, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    int findResult(WikipediaPage page, int start, List<MatchResult> matches);
}
