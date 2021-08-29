package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.immutable.ImmutableFinderPriority;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

@Slf4j
abstract class QuotesFinder extends ImmutableCheckedFinder {

    private static final char NEW_LINE = '\n';
    private static final Set<Character> FORBIDDEN_CHARS = Set.of('\n', '#', '{', '}', '<', '>');
    private static final Set<Character> QUOTE_CHARS = Set.of('«', '»', '"', '“', '”');

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.LOW;
    }

    @Override
    public int getMaxLength() {
        return 1000;
    }

    abstract char getStartChar();

    abstract char getEndChar();

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return LinearMatchFinder.find(page, this::findResult);
    }

    @Nullable
    private MatchResult findResult(FinderPage page, int start) {
        List<MatchResult> matches = new ArrayList<>();
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findQuote(page, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findQuote(FinderPage page, int start, List<MatchResult> matches) {
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

                LinearMatchResult linearMatchResult = LinearMatchResult.of(startQuote, quotedText);

                // Check the quoted text is not empty and is not an attribute
                String innerText = quotedText.substring(1, quotedText.length() - 1);
                if (StringUtils.isBlank(innerText) && text.charAt(startQuote - 1) != '=') {
                    Immutable immutable = Immutable.of(
                        startQuote,
                        FinderUtils.getContextAroundWord(text, startQuote, endQuote, CONTEXT_THRESHOLD)
                    );
                    logWarning(immutable, page, "Empty quoted text");
                    return endQuote + 1;
                }

                // Check the quoted text is not quoted again
                if (
                    StringUtils.isNotBlank(innerText) &&
                    QUOTE_CHARS.contains(innerText.charAt(0)) &&
                    QUOTE_CHARS.contains(innerText.charAt(innerText.length() - 1))
                ) {
                    logWarning(convert(linearMatchResult), page, "Redundant quotes");
                    // Continue
                }

                matches.add(linearMatchResult);
                return endQuote + 1;
            } else {
                // No quote ending found
                // It's possible that the quote start was a false positive
                Immutable immutable = Immutable.of(
                    startQuote,
                    FinderUtils.getContextAroundWord(text, startQuote, startQuote + 1, CONTEXT_THRESHOLD)
                );
                logWarning(immutable, page, "Truncated quotes");
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
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == NEW_LINE) {
                return -1;
            } else if (ch == getEndChar()) {
                return i;
            }
        }
        return -1;
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
