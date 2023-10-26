package es.bvalero.replacer.finder.immutable.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.NEW_LINE;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.Set;
import java.util.regex.MatchResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

abstract class QuotesFinder extends ImmutableCheckedFinder {

    protected static final char DOUBLE_QUOTES = '\"';
    protected static final char START_QUOTE_ANGULAR = '«';
    protected static final char END_QUOTE_ANGULAR = '»';
    protected static final char START_QUOTE_TYPOGRAPHIC = '“';
    protected static final char END_QUOTE_TYPOGRAPHIC = '”';
    private static final Set<Character> QUOTE_CHARS = Set.of(
        DOUBLE_QUOTES,
        START_QUOTE_ANGULAR,
        END_QUOTE_ANGULAR,
        START_QUOTE_TYPOGRAPHIC,
        END_QUOTE_TYPOGRAPHIC
    );

    @Override
    public FinderPriority getPriority() {
        return FinderPriority.LOW;
    }

    @Override
    public int getMaxLength() {
        return 1000;
    }

    abstract char getStartChar();

    abstract char getEndChar();

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // The linear approach is 3x faster than the most optimized automaton
        return LinearMatchFinder.find(page, this::findQuote);
    }

    @Nullable
    private MatchResult findQuote(FinderPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            final int startQuote = findStartQuote(text, start);
            if (startQuote < 0) {
                return null;
            }

            final int startQuoteText = startQuote + 1;
            final int endQuote = findEndQuote(text, startQuoteText);
            if (endQuote < 0) {
                // No quote ending found
                // It's possible that the quote start was a false positive or the quote contains new lines
                logImmutableCheck(page, startQuote, startQuoteText, "Truncated quotes");
                start = startQuoteText;
                continue;
            }

            if (validateQuote(page, startQuote, endQuote)) {
                return LinearMatchResult.of(text, startQuote, endQuote);
            } else {
                start = endQuote;
            }
        }
        return null;
    }

    private int findStartQuote(String text, int start) {
        return text.indexOf(getStartChar(), start);
    }

    private int findEndQuote(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            final char ch = text.charAt(i);
            if (ch == NEW_LINE) {
                // New lines are not allowed inside quotes
                return -1;
            } else if (ch == getEndChar()) {
                return i + 1;
            }
        }
        return -1;
    }

    protected boolean validateQuote(FinderPage page, int startQuote, int endQuote) {
        final String quoteText = page.getContent().substring(startQuote + 1, endQuote - 1);

        // Check if the quote is empty
        if (isEmptyQuote(quoteText)) {
            if (isEmptyQuoteWarning(page, startQuote, endQuote)) {
                logImmutableCheck(page, startQuote, endQuote, "Empty quoted text");
            }
            return false;
        }

        // Check the quote text is not quoted again by checking the first and last characters are not quote characters
        if (isRedundantQuote(quoteText)) {
            logImmutableCheck(page, startQuote, endQuote, "Redundant quotes");
        }

        return true;
    }

    private boolean isEmptyQuote(String quoteText) {
        return StringUtils.isBlank(quoteText);
    }

    protected boolean isEmptyQuoteWarning(FinderPage page, int startQuote, int endQuote) {
        return true;
    }

    private boolean isRedundantQuote(String quoteText) {
        return (
            StringUtils.isNotEmpty(quoteText) &&
            isQuoteChar(quoteText.charAt(0)) &&
            isQuoteChar(quoteText.charAt(quoteText.length() - 1))
        );
    }

    private boolean isQuoteChar(char ch) {
        return QUOTE_CHARS.contains(ch);
    }
}
