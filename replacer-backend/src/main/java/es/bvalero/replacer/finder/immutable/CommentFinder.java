package es.bvalero.replacer.finder.immutable;

import java.util.regex.Pattern;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.RegexIterable;
import org.springframework.stereotype.Component;

/**
 * Find XML comments, e. g. `<!-- A comment -->`
 */
@Component
class CommentFinder implements ImmutableFinder {
    private static final String REGEX_COMMENT_TAG = "<!--.+?-->";
    private static final Pattern PATTERN_COMMENT_TAG = Pattern.compile(REGEX_COMMENT_TAG, Pattern.DOTALL);

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<>(text, PATTERN_COMMENT_TAG, this::convert);
    }
}
