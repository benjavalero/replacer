package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.ImmutableFinder;
import org.springframework.stereotype.Component;

/**
 * Find text in typographic quotes, e.g. `“text”`. The text may include new lines.
 */
@Component
public class QuotesTypographicFinder extends QuotesAbstractFinder implements ImmutableFinder {

    @Override
    char getStartChar() {
        return '“';
    }

    @Override
    char getEndChar() {
        return '”';
    }
}
