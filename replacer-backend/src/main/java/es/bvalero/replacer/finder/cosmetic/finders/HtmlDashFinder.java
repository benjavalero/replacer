package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Find n-dash and m-dash HTML entities */
@Component
class HtmlDashFinder extends CosmeticCheckedFinder {

    @RegExp
    private static final String REGEX_HTML_DASH = "&[mn]dash;";

    private static final Pattern PATTERN_HTML_DASH = Pattern.compile(REGEX_HTML_DASH);

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), PATTERN_HTML_DASH);
    }

    @Override
    public CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.HTML_DASH;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        return match.group().replace("&mdash;", "\u2014").replace("&ndash;", "\u2013");
    }
}
