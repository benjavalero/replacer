package es.bvalero.replacer.finder.immutable;

import org.springframework.stereotype.Component;

/**
 * Find text in double quotes, e.g. `"text"`. The text may include new lines.
 */
@Component
class QuotesDoubleFinder extends QuotesFinder implements ImmutableFinder {

    @Override
    char getStartChar() {
        return '"';
    }

    @Override
    char getEndChar() {
        return '"';
    }
}
