package es.bvalero.replacer.finder.immutable.finders;

import com.roklenarcic.util.strings.MapMatchListener;
import es.bvalero.replacer.finder.util.FinderUtils;

/**
 * Listener for the Aho-Corasick algorithm in redirection finders.
 * Unlike the generic {@link es.bvalero.replacer.finder.util.ResultMatchListener}, this one stops on the first match
 * ({@code return false}) since redirection only needs to know whether any word is present, not all occurrences.
 * It also validates word completeness at match time, since the match result returned to the caller
 * covers the whole page (start=0) and cannot be used for positional validation afterwards.
 */
public class RedirectionMatchListener implements MapMatchListener<String> {

    private boolean found = false;

    public boolean isFound() {
        return found;
    }

    @Override
    public boolean match(String text, int start, int end, String word) {
        if (FinderUtils.isWordCompleteInText(start, end, text)) {
            found = true;
            return false;
        }
        return true;
    }
}
