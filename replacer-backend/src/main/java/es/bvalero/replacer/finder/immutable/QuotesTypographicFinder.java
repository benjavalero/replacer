package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Find text in typographic quotes, e. g. `“text”`
 */
@Component
public class QuotesTypographicFinder implements ImmutableFinder {

    @Override
    public Iterable<Immutable> find(String text) {
        List<Immutable> matches = new ArrayList<>(100);
        int start = 0;
        while (start >= 0) {
            start = findQuote(text, start, matches);
        }
        return matches;
    }

    private int findQuote(String text, int start, List<Immutable> matches) {
        int openQuote = text.indexOf('“', start);
        if (openQuote >= 0) {
            int endQuote = text.indexOf('”', openQuote + 1);
            if (endQuote >= 0) {
                matches.add(Immutable.of(openQuote, text.substring(openQuote, endQuote + 1)));
                return endQuote + 1;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }
}
