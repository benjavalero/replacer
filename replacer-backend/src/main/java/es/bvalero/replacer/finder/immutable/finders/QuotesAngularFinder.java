package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.immutable.ImmutableFinder;
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
