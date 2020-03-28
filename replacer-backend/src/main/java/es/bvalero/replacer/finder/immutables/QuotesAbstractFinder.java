package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.LinearIterable;
import es.bvalero.replacer.finder.LinearMatcher;

import java.util.regex.MatchResult;

abstract class QuotesAbstractFinder implements ImmutableFinder {

    abstract char getStartChar();

    abstract char getEndChar();

    @Override
    public Iterable<Immutable> find(String text) {
        return new LinearIterable<>(text, this::findQuote, this::convert);
    }

    private MatchResult findQuote(String text, int start) {
        int openQuote = text.indexOf(getStartChar(), start);
        if (openQuote >= 0) {
            int endQuote = text.indexOf(getEndChar(), openQuote + 1);
            if (endQuote >= 0) {
                return LinearMatcher.of(openQuote, text.substring(openQuote, endQuote + 1));
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
