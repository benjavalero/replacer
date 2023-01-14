package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
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
    public FinderPriority getPriority() {
        return FinderPriority.LOW;
    }

    @Override
    public int getMaxLength() {
        return 10000;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return LinearMatchFinder.find(page, this::findComment);
    }

    @Nullable
    private MatchResult findComment(FinderPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            final int startComment = findStartComment(text, start);
            if (startComment < 0) {
                break;
            }

            final int startCommentText = startComment + START_COMMENT.length();
            final int endComment = findEndComment(text, startCommentText);
            if (endComment < 0) {
                // Comment not closed. Trace warning and continue.
                FinderUtils.logFinderResult(page, startComment, startCommentText, "Comment not closed");
                start = startCommentText;
                continue;
            }

            return LinearMatchResult.of(startComment, text.substring(startComment, endComment));
        }
        return null;
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
