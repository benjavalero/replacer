package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.LinearIterable;
import es.bvalero.replacer.finder.LinearMatcher;
import es.bvalero.replacer.page.IndexablePage;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find XML comments, e.g. `<!-- A comment -->`
 */
@Slf4j
@Component
public class CommentFinder implements ImmutableFinder {

    private static final String START_COMMENT = "<!--";
    private static final String END_COMMENT = "-->";

    @Override
    public int getMaxLength() {
        return 10000;
    }

    @Override
    public Iterable<Immutable> find(IndexablePage page) {
        return new LinearIterable<>(page, this::findResult, this::convert);
    }

    @Nullable
    public MatchResult findResult(IndexablePage page, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findComment(page, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findComment(IndexablePage page, int start, List<MatchResult> matches) {
        String text = page.getContent();
        int startComment = findStartComment(text, start);
        if (startComment >= 0) {
            int endComment = findEndComment(text, startComment + START_COMMENT.length());
            if (endComment >= 0) {
                int endCommentComplete = endComment + END_COMMENT.length();
                matches.add(LinearMatcher.of(startComment, text.substring(startComment, endCommentComplete)));
                return endCommentComplete;
            } else {
                // Comment not closed. Not worth keep on searching.
                Immutable immutable = Immutable.of(
                    startComment,
                    text.substring(startComment, startComment + 100),
                    this
                );
                logWarning(immutable, page, LOGGER, "Comment not closed");
                return -1;
            }
        } else {
            return -1;
        }
    }

    private int findStartComment(String text, int start) {
        return text.indexOf(START_COMMENT, start);
    }

    private int findEndComment(String text, int start) {
        return text.indexOf(END_COMMENT, start);
    }
}
