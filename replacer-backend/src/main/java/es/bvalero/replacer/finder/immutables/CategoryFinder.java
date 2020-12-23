package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.*;
import es.bvalero.replacer.page.IndexablePage;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find categories, e.g. `[[Categoría:España]]`
 */
@Slf4j
@Component
public class CategoryFinder implements ImmutableFinder {

    private static final String CATEGORY_START = "[[Categoría:";
    private static final String CATEGORY_END = "]]";

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.LOW;
    }

    @Override
    public int getMaxLength() {
        return 250;
    }

    @Override
    public Iterable<Immutable> find(IndexablePage page) {
        return new LinearIterable<>(page, this::findResult, this::convert);
    }

    @Nullable
    public MatchResult findResult(IndexablePage page, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findCategory(page, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findCategory(IndexablePage page, int start, List<MatchResult> matches) {
        String text = page.getContent();
        int startCategory = findStartCategory(text, start);
        if (startCategory >= 0) {
            int startCategoryName = startCategory + CATEGORY_START.length();
            int endCategory = findEndCategory(text, startCategoryName);
            if (endCategory >= 0) {
                int endMatch = endCategory + CATEGORY_END.length();
                matches.add(LinearMatcher.of(startCategory, text.substring(startCategory, endMatch)));
                return endMatch;
            } else {
                // Category not closed. Not worth keep on searching.
                Immutable immutable = Immutable.of(startCategory, text.substring(startCategory, startCategory + 100), this);
                logWarning(immutable, page, LOGGER, "Category not closed");
                return -1;
            }
        } else {
            // No more categories
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
