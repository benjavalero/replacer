package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.parser.Comment;
import es.bvalero.replacer.finder.parser.ExpressionType;
import es.bvalero.replacer.finder.parser.FinderParserPage;
import es.bvalero.replacer.finder.parser.Parser;
import java.util.regex.MatchResult;
import org.springframework.stereotype.Component;

/**
 * Find XML comments, e.g. `<!-- A comment -->`
 */
@Component
class CommentFinder extends ImmutableCheckedFinder {

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
        assert page instanceof FinderParserPage;
        final Parser parser = ((FinderParserPage) page).getParser();
        return parser.find(page.getContent(), ExpressionType.COMMENT, exp -> {
            assert exp instanceof Comment;
            final Comment comment = (Comment) exp;
            if (comment.isTruncated()) {
                logImmutableCheck(page, comment.start(), comment.end(), "Comment not closed");
            }
            return exp;
        });
    }
}
