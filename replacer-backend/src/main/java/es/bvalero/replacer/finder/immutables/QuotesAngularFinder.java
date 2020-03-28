package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.LinearIterable;
import es.bvalero.replacer.finder.LinearMatcher;
import java.util.regex.MatchResult;
import org.springframework.stereotype.Component;

/**
 * Find text in angular quotes, e. g. `«text»`
 */
@Component
public class QuotesAngularFinder extends QuotesAbstractFinder implements ImmutableFinder {
    @Override
    char getStartChar() {
        return '«';
    }

    @Override
    char getEndChar() {
        return '»';
    }}
