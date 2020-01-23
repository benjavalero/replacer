package es.bvalero.replacer.finder;

import es.bvalero.replacer.finder2.Immutable;
import es.bvalero.replacer.finder2.ImmutableFinder;
import java.util.Iterator;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Find XML comments, e. g. `<!-- A comment -->`
 */
@Component
class CommentFinder implements ImmutableFinder {
    private static final String REGEX_COMMENT_TAG = "<!--.+?-->";
    private static final Pattern PATTERN_COMMENT_TAG = Pattern.compile(REGEX_COMMENT_TAG, Pattern.DOTALL);

    @Override
    public Iterator<Immutable> find(String text) {
        return find(text, PATTERN_COMMENT_TAG);
    }
}
