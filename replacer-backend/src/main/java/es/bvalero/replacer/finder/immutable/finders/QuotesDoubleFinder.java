package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import java.util.Collection;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Find text in double quotes, e.g. `"text"`. The text may include new lines.
 */
@Component
class QuotesDoubleFinder extends QuotesFinder implements ImmutableFinder {

    private static final Set<Character> FORBIDDEN_CHARS = Set.of('#', '{', '}', '<', '>');

    @Override
    char getStartChar() {
        return '"';
    }

    @Override
    char getEndChar() {
        return '"';
    }

    @Override
    Collection<Character> getForbiddenChars() {
        return FORBIDDEN_CHARS;
    }
}
