package es.bvalero.replacer.finder.immutable;

import org.springframework.stereotype.Component;

/**
 * Find text in typographic quotes, e.g. `“text”`. The text may include new lines.
 */
@Component
class QuotesTypographicFinder extends QuotesFinder implements ImmutableFinder {

    @Override
    char getStartChar() {
        return '“';
    }

    @Override
    char getEndChar() {
        return '”';
    }
}
