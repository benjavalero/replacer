package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import org.apache.commons.lang3.StringUtils;

abstract class QuotesFinder extends ImmutableCheckedFinder {

    protected static final char DOUBLE_QUOTES = '\"';
    protected static final char START_QUOTE_ANGULAR = '«';
    protected static final char END_QUOTE_ANGULAR = '»';
    protected static final char START_QUOTE_TYPOGRAPHIC = '“';
    protected static final char END_QUOTE_TYPOGRAPHIC = '”';
    private static final char NEW_LINE = '\n';
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
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return LinearMatchFinder.find(page, this::findQuote);
    }

    private int findQuote(WikipediaPage page, int start, List<MatchResult> matches) {
        final String text = page.getContent();
        final int startQuote = findStartQuote(text, start);
        if (startQuote >= 0) {
            final int startQuoteText = startQuote + 1; // 1 = start quote length
            final int endQuoteText = findEndQuote(text, startQuoteText);
            if (endQuoteText >= 0) {
                final String quoteText = text.substring(startQuoteText, endQuoteText);
                if (!isValidQuoteText(quoteText)) {
                    return endQuoteText;
                }

                final int endQuote = endQuoteText + 1; // 1 = end quote length
                if (isEmptyQuote(quoteText, startQuote, text, page)) {
                    return endQuote;
                }

                checkRedundantQuote(quoteText, startQuote, page);

                final String quote = text.substring(startQuote, endQuote);
                matches.add(LinearMatchResult.of(startQuote, quote));
                return endQuote;
            } else {
                // No quote ending found
                // It's possible that the quote start was a false positive or the quote contains new lines
                logImmutableCheck(page, startQuote, startQuoteText, "Truncated quotes");
                return startQuoteText;
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
            final char ch = text.charAt(i);
            if (ch == NEW_LINE) {
                // New lines are not allowed inside quotes
                return -1;
            } else if (ch == getEndChar()) {
                return i;
            }
        }
        return -1;
    }

    private boolean isValidQuoteText(String text) {
        return !containsForbiddenChars(text);
    }

    private boolean containsForbiddenChars(String text) {
        return text.chars().mapToObj(c -> (char) c).anyMatch(this::isForbiddenChar);
    }

    private boolean isForbiddenChar(char ch) {
        return getForbiddenChars().contains(ch);
    }

    Collection<Character> getForbiddenChars() {
        return Collections.emptySet();
    }

    private boolean isEmptyQuote(String quoteText, int startQuote, String text, WikipediaPage page) {
        // Warning when empty but when it is an attribute
        final boolean isEmpty = StringUtils.isBlank(quoteText);
        if (isEmpty && !isAttribute(startQuote, text)) {
            final int endQuote = startQuote + quoteText.length() + 2; // 2 = start + end quote length
            logImmutableCheck(page, startQuote, endQuote, "Empty quoted text");
        }
        return isEmpty;
    }

    private boolean isAttribute(int startQuote, String text) {
        return startQuote > 0 && text.charAt(startQuote - 1) == '=';
    }

    private void checkRedundantQuote(String quoteText, int startQuote, WikipediaPage page) {
        // Check the quote text is not quoted again by checking the first and last characters are not quote characters
        if (
            StringUtils.isNotBlank(quoteText) &&
            isQuoteChar(quoteText.charAt(0)) &&
            isQuoteChar(quoteText.charAt(quoteText.length() - 1))
        ) {
            final int endQuote = startQuote + quoteText.length() + 2; // 2 = start + end quote length
            logImmutableCheck(page, startQuote, endQuote, "Redundant quotes");
        }
    }

    private boolean isQuoteChar(char ch) {
        return QUOTE_CHARS.contains(ch);
    }
}
