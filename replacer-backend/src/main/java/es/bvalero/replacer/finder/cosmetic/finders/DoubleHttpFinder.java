package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Find external links with double HTTP */
@Component
class DoubleHttpFinder extends CosmeticCheckedFinder {

    @RegExp
    private static final String REGEX_DOUBLE_HTTP = "(https?://)\\1(\\S*)";

    private static final Pattern PATTERN_DOUBLE_HTTP = Pattern.compile(REGEX_DOUBLE_HTTP);

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), PATTERN_DOUBLE_HTTP);
    }

    @Override
    public CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.EXTERNAL_LINK_WITH_DOUBLE_HTTP;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        return match.group(1) + match.group(2);
    }
}
