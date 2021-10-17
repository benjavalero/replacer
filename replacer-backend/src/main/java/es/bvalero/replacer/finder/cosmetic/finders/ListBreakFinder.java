package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Find list items ending with a break */
@Component
class ListBreakFinder extends CosmeticCheckedFinder {

    @RegExp
    private static final String REGEX_LIST_BREAK = "^(\\*.+?)<br ?/?>$";

    private static final Pattern PATTERN_LIST_BREAK = Pattern.compile(REGEX_LIST_BREAK, Pattern.MULTILINE);

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), PATTERN_LIST_BREAK);
    }

    @Override
    public CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.BREAK_IN_LIST;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        return match.group(1).trim();
    }
}
