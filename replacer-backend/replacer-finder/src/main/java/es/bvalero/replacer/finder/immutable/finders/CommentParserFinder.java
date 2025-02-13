package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.parser.*;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import java.util.List;
import java.util.regex.MatchResult;
import org.springframework.stereotype.Component;

/**
 * Find XML comments, e.g. `<!-- A comment -->`
 */
@Component
class CommentParserFinder extends ImmutableCheckedFinder {

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
        Parser parser = new Parser();
        List<Expression> expressions = parser.parse(page.getContent());

        // TODO: Iterate through all the tree
        return expressions
            .stream()
            .filter(e -> e.type() == ExpressionType.COMMENT)
            .map(e -> (MatchResult) FinderMatchResult.of(page.getContent(), e.start(), e.end()))
            .toList();
    }
}
