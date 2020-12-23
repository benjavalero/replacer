package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.LinearIterable;
import es.bvalero.replacer.finder.LinearMatcher;
import es.bvalero.replacer.page.IndexablePage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

@Slf4j
abstract class QuotesAbstractFinder implements ImmutableFinder {

    private static final Set<Character> FORBIDDEN_CHARS = Set.of('\n', '#', '{', '}', '<', '>');
    private static final Set<Character> QUOTE_CHARS = Set.of('«', '»', '"', '“', '”');

    @Override
    public int getMaxLength() {
        return 1000;
    }

    abstract char getStartChar();

    abstract char getEndChar();

    @Override
    public Iterable<Immutable> find(IndexablePage page) {
        return new LinearIterable<>(page, this::findResult, this::convert);
    }

    @Nullable
    public MatchResult findResult(IndexablePage page, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findQuote(page, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findQuote(IndexablePage page, int start, List<MatchResult> matches) {
        String text = page.getContent();
        int startQuote = findStartQuote(text, start);
        if (startQuote >= 0) {
            int endQuote = findEndQuote(text, startQuote + 1);
            if (endQuote >= 0) {
                String quotedText = text.substring(startQuote, endQuote + 1);

                if (!validateForbiddenChars(quotedText)) {
                    // Quote containing new lines or other forbidden chars. Skipping.
                    return endQuote + 1;
                }

                LinearMatcher linearMatcher = LinearMatcher.of(startQuote, quotedText);

                // Check the quoted text is not empty and is not quoted again
                String innerText = quotedText.substring(1, quotedText.length() - 1);
                if (StringUtils.isBlank(innerText)) {
                    logWarning(convert(linearMatcher), page, LOGGER, "Empty quoted text");
                    return endQuote + 1;
                }

                if (
                    QUOTE_CHARS.contains(innerText.charAt(0)) ||
                    QUOTE_CHARS.contains(innerText.charAt(innerText.length() - 1))
                ) {
                    logWarning(convert(linearMatcher), page, LOGGER, "Redundant quotes");
                    // Continue
                }

                matches.add(linearMatcher);
                return endQuote + 1;
            } else {
                // No quote ending found
                // It's possible that the quote start was a false positive
                return startQuote + 1;
            }
        } else {
            return -1;
        }
    }

    private int findStartQuote(String text, int start) {
        return text.indexOf(getStartChar(), start);
    }

    private int findEndQuote(String text, int start) {
        return text.indexOf(getEndChar(), start);
    }

    /* Check if the found text contains any forbidden char */
    private boolean validateForbiddenChars(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (FORBIDDEN_CHARS.contains(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
