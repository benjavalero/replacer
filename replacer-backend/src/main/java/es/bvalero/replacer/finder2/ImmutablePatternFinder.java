package es.bvalero.replacer.finder2;

import java.util.Iterator;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper abstract class to find immutables using a regex pattern.
 */
public abstract class ImmutablePatternFinder implements ImmutableFinder {

    public abstract Pattern getPattern();

    public Immutable getImmutableFromResult(MatchResult matcher) {
        return Immutable.of(matcher.start(), matcher.group());
    }

    @Override
    public Iterator<Immutable> findImmutables(String text) {
        Matcher matcher = getPattern().matcher(text);
        return new Iterator<Immutable>() {

            @Override
            public boolean hasNext() {
                return matcher.find();
            }

            @Override
            public Immutable next() {
                return getImmutableFromResult(matcher);
            }
        };
    }
}
