package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.ImmutableFinderPriority;
import org.springframework.stereotype.Component;

/**
 * Find text in double quotes, e.g. `"text"`
 */
@Component
public class QuotesFinder extends QuotesAbstractFinder implements ImmutableFinder {
    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.LOW;
    }

    @Override
    public int getMaxLength() {
        return 5000;
    }

    @Override
    char getStartChar() {
        return '"';
    }

    @Override
    char getEndChar() {
        return '"';
    }
}
