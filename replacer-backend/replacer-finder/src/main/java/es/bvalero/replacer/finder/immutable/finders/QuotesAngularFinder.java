package es.bvalero.replacer.finder.immutable.finders;

import org.springframework.stereotype.Component;

/**
 * Find text in angular quotes, e.g. `«text»`. The text may include new lines.
 */
@Component
public class QuotesAngularFinder extends QuotesFinder {

    @Override
    char getStartChar() {
        return START_QUOTE_ANGULAR;
    }

    @Override
    char getEndChar() {
        return END_QUOTE_ANGULAR;
    }
}
