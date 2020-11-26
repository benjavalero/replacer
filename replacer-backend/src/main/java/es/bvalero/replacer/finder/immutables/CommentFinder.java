package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.LinearIterable;
import es.bvalero.replacer.finder.LinearMatcher;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find XML comments, e.g. `<!-- A comment -->`
 */
@Component
public class CommentFinder implements ImmutableFinder {

    @Override
    public int getMaxLength() {
        return 10000;
    }

    @Override
    public Iterable<Immutable> find(String text, WikipediaLanguage lang) {
        return new LinearIterable<>(text, this::findResult, this::convert);
    }

    @Nullable
    public MatchResult findResult(String text, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && matches.isEmpty()) {
            start = findComment(text, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findComment(String text, int start, List<MatchResult> matches) {
        int startComment = findStartComment(text, start);
        if (startComment >= 0) {
            int endComment = findEndComment(text, startComment + 4);
            if (endComment >= 0) {
                matches.add(LinearMatcher.of(startComment, text.substring(startComment, endComment + 3)));
                return endComment + 3;
            } else {
                return startComment + 4;
            }
        } else {
            return -1;
        }
    }

    private int findStartComment(String text, int start) {
        return text.indexOf("<!--", start);
    }

    private int findEndComment(String text, int start) {
        return text.indexOf("-->", start);
    }
}
