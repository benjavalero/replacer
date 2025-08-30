package es.bvalero.replacer.finder.benchmark.iterable;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.LinearFinder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ListMatchFinder {

    public static List<MatchResult> find(FinderPage page, LinearFinder finder) {
        final List<MatchResult> results = new ArrayList<>(100);
        int start = 0;
        while (start >= 0) {
            final MatchResult m = finder.findResult(page, start);
            if (m == null) {
                break;
            } else {
                results.add(m);
                start = m.end();
            }
        }

        return results;
    }
}
