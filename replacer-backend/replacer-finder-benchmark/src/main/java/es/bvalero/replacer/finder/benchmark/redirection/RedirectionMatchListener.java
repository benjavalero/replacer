package es.bvalero.replacer.finder.benchmark.redirection;

import com.roklenarcic.util.strings.MapMatchListener;
import es.bvalero.replacer.finder.util.FinderUtils;

/**
 * Listener for the Aho-Corasick algorithm in redirection benchmark finders.
 */
class RedirectionMatchListener implements MapMatchListener<String> {

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
