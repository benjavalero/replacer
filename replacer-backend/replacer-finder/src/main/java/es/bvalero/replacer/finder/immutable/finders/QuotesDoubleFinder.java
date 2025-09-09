package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import org.springframework.stereotype.Component;

/**
 * Find text in double quotes, e.g. `"text"`. The text may include new lines.
 */
@Component
class QuotesDoubleFinder extends QuotesFinder implements ImmutableFinder {

    @Override
    char getStartChar() {
        return DOUBLE_QUOTES;
    }

    @Override
    char getEndChar() {
        return DOUBLE_QUOTES;
    }

    @Override
    public boolean validateQuote(FinderPage page, int startQuote, int endQuote) {
        if (super.validateQuote(page, startQuote, endQuote)) {
            return isValidQuoteText(page, startQuote, endQuote);
        } else {
            return false;
        }
    }

    private boolean isValidQuoteText(FinderPage page, int startQuote, int endQuote) {
        return !containsForbiddenChars(page.getContent(), startQuote + 1, endQuote - 1);
    }

    private boolean containsForbiddenChars(String text, int start, int end) {
        for (int i = start; i < end; i++) {
            if (isForbiddenChar(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean isForbiddenChar(char ch) {
        return ch == '#' || ch == '{' || ch == '}' || ch == '<' || ch == '>';
    }

    @Override
    public boolean isEmptyQuoteWarning(FinderPage page, int startQuote, int endQuote) {
        // Warning when empty but when it is an attribute
        return !isAttribute(page.getContent(), startQuote);
    }

    private boolean isAttribute(String text, int startQuote) {
        return startQuote > 0 && text.charAt(startQuote - 1) == '=';
    }
}
