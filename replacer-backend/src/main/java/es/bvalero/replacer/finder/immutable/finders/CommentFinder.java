package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.immutable.ImmutableFinderPriority;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find XML comments, e.g. `<!-- A comment -->`
 */
@Component
class CommentFinder extends ImmutableCheckedFinder {

    private static final String START_COMMENT = "<!--";
    private static final String END_COMMENT = "-->";

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.LOW;
    }

    @Override
    public int getMaxLength() {
        return 10000;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return LinearMatchFinder.find(page, this::findResult);
    }

    @Nullable
    private MatchResult findResult(FinderPage page, int start) {
        final List<MatchResult> matches = new ArrayList<>();
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findComment(page, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findComment(FinderPage page, int start, List<MatchResult> matches) {
        final String text = page.getContent();
        final int startComment = findStartComment(text, start);
        if (startComment >= 0) {
            final int startCommentText = startComment + START_COMMENT.length();
            final int endComment = findEndComment(text, startCommentText);
            if (endComment >= 0) {
                matches.add(LinearMatchResult.of(startComment, text.substring(startComment, endComment)));
                return endComment;
            } else {
                // Comment not closed. Trace warning and continue.
                FinderUtils.logFinderResult(
                    getImmutableSnippet(startComment, startCommentText, page),
                    "Comment not closed"
                );
                return startCommentText;
            }
        } else {
            return -1;
        }
    }

    private int findStartComment(String text, int start) {
        return text.indexOf(START_COMMENT, start);
    }

    private int findEndComment(String text, int start) {
        final int posTagEndComment = text.indexOf(END_COMMENT, start);
        if (posTagEndComment >= 0) {
            return posTagEndComment + END_COMMENT.length();
        } else {
            return -1;
        }
    }
}
