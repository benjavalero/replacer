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
        // To match the content of the comment with a regex we cannot use a negated class,
        // as we want to capture dashes or tags inside.
        // The alternative is using the classic .+? approach (the lazy modifier is needed),
        // but this modifier is only supported by the Regex matcher, which is 10x slower.
        return LinearMatchFinder.find(page, this::findComment);
    }

    @Nullable
    private MatchResult findComment(FinderPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            final int startComment = findStartComment(text, start);
            if (startComment < 0) {
                return null;
            }

            final int startCommentText = startComment + START_COMMENT.length();
            final int endComment = findEndComment(text, startCommentText);
            if (endComment < 0) {
                // Comment not closed. Trace warning and continue.
                FinderUtils.logFinderResult(page, startComment, startCommentText, "Comment not closed");
                start = startCommentText;
                continue;
            }

            return LinearMatchResult.of(text, startComment, endComment);
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
