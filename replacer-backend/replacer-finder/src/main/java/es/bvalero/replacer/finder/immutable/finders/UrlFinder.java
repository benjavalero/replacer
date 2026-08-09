package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.FinderMatchRange;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find URLs, e.g. `https://www.google.es`
 */
@Component
public class UrlFinder implements ImmutableFinder {

    private static final String HTTP = "http";
    private static final String SCHEME_SUFFIX = "://";

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return LinearMatchFinder.find(page, this::findUrl);
    }

    @Nullable
    private MatchResult findUrl(FinderPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            final int startHttp = text.indexOf(HTTP, start);
            if (startHttp < 0) {
                return null;
            }

            // Check optional 's' after "http"
            int pos = startHttp + HTTP.length();
            if (pos < text.length() && text.charAt(pos) == 's') {
                pos++;
            }

            // Must be followed by "://"
            if (!text.startsWith(SCHEME_SUFFIX, pos)) {
                start = startHttp + 1;
                continue;
            }
            pos += SCHEME_SUFFIX.length();

            // Consume valid URL characters
            final int endUrl = findEndUrl(text, pos);
            if (endUrl == pos) {
                // Nothing after "://" — not a valid URL
                start = pos;
                continue;
            }

            return FinderMatchRange.of(text, startHttp, endUrl);
        }
        return null;
    }

    // Advance while the character is a valid URL character (not a wiki/HTML delimiter or whitespace)
    private int findEndUrl(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            if (isUrlEndChar(text.charAt(i))) {
                return i;
            }
        }
        return text.length();
    }

    private boolean isUrlEndChar(char ch) {
        return (
            Character.isWhitespace(ch) ||
            ch == ']' ||
            ch == '[' ||
            ch == '}' ||
            ch == '<' ||
            ch == '>' ||
            ch == '"' ||
            ch == '\''
        );
    }

    @Override
    public FinderPriority getPriority() {
        return FinderPriority.MEDIUM;
    }
}
