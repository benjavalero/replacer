package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinder;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Double small tags which make the text too tiny and less accessible, e.g. `<small><small>Text</small></small> ==> <small>Text</small>` */
@Component
class DoubleSmallTagFinder implements CosmeticFinder {

    @RegExp
    private static final String REGEX_DOUBLE_SMALL_TAG = "<small><small>(.+?)</small></small>";

    private static final Pattern PATTERN_DOUBLE_SMALL_TAG = Pattern.compile(REGEX_DOUBLE_SMALL_TAG);

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), PATTERN_DOUBLE_SMALL_TAG);
    }

    @Override
    public CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.DOUBLE_SMALL_TAG;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        return String.format("<small>%s</small>", match.group(1));
    }
}
