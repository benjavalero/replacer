package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.ImmutableFinder;
import org.springframework.stereotype.Component;

/**
 * Find text in double quotes, e. g. `"text"`
 */
@Component
public class QuotesFinder extends QuotesAbstractFinder implements ImmutableFinder {
    @Override
    char getStartChar() {
        return '"';
    }

    @Override
    char getEndChar() {
        return '"';
    }
}
