package es.bvalero.replacer.finder.immutable;

import org.springframework.stereotype.Component;

/**
 * Find text in angular quotes, e.g. `«text»`. The text may include new lines.
 */
@Component
class QuotesAngularFinder extends QuotesFinder implements ImmutableFinder {

    @Override
    char getStartChar() {
        return '«';
    }

    @Override
    char getEndChar() {
        return '»';
    }
}
