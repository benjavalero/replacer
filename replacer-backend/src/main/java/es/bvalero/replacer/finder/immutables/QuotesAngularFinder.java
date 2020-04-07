package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.ImmutableFinder;
import org.springframework.stereotype.Component;

/**
 * Find text in angular quotes, e. g. `«text»`
 */
@Component
public class QuotesAngularFinder extends QuotesAbstractFinder implements ImmutableFinder {
    @Override
    public int getMaxLength() {
        return 200;
    }

    @Override
    char getStartChar() {
        return '«';
    }

    @Override
    char getEndChar() {
        return '»';
    }
}
