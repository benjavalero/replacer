package es.bvalero.replacer.finder.benchmark.category;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;

class CategoryLinearFinder implements BenchmarkFinder {

    private static final String CATEGORY_START = "[[Categoría:";
    private static final String CATEGORY_END = "]]";

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return LinearMatchFinder.find(page, this::findResult);
    }

    @Nullable
    private MatchResult findResult(FinderPage page, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findCategory(page.getContent(), start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findCategory(String text, int start, List<MatchResult> matches) {
        int startCategory = findStartCategory(text, start);
        if (startCategory >= 0) {
            int startCategoryName = startCategory + CATEGORY_START.length();
            int endCategory = findEndCategory(text, startCategoryName);
            if (endCategory >= 0) {
                int endMatch = endCategory + CATEGORY_END.length();
                matches.add(LinearMatchResult.of(startCategory, text.substring(startCategory, endMatch)));
                return endMatch;
            } else {
                return startCategoryName;
            }
        } else {
            return -1;
        }
    }

    private int findStartCategory(String text, int start) {
        return text.indexOf(CATEGORY_START, start);
    }

    private int findEndCategory(String text, int start) {
        return text.indexOf(CATEGORY_END, start);
    }
}
