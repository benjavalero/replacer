package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import org.springframework.stereotype.Component;

/**
 * Find text in typographic quotes, e.g. `“text”`. The text may include new lines.
 */
@Component
class QuotesTypographicFinder extends QuotesFinder implements ImmutableFinder {

    @Override
    char getStartChar() {
        return START_QUOTE_TYPOGRAPHIC;
    }

    @Override
    char getEndChar() {
        return END_QUOTE_TYPOGRAPHIC;
    }
}
