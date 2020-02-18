package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Find web domains, e. g. `www.acb.es` or `es.wikipedia.org`
 */
@Component
public class DomainFinder implements ImmutableFinder {

    @Override
    public Iterable<Immutable> find(String text) {
        List<Immutable> matches = new ArrayList<>(100);
        int start = 0;
        while (start >= 0) {
            // Find a letter
            char ch = text.charAt(start);
            while (!isAscii(ch)) {
                start++;
                if (start == text.length()) return matches;
                ch = text.charAt(start);
            }
            // Find while there are letters or dots
            int startDomain = start;
            boolean dotFound = false;
            while (start < text.length() - 1 && (isAscii(ch) || ch == '.')) {
                if (ch == '.') dotFound = true;
                start++;
                ch = text.charAt(start);
            }
            if (dotFound) {
                matches.add(Immutable.of(startDomain, text.substring(startDomain, start)));
            }
            if (start >= text.length() - 1) break;
        }
        return matches;
    }

    private boolean isAscii(char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
    }
}
