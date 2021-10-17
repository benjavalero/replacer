package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Find break with incorrect syntax */
@Component
class BreakIncorrectFinder extends CosmeticCheckedFinder {

    @RegExp
    private static final String REGEX_BREAK = "</br>";

    private static final Pattern PATTERN_BREAK = Pattern.compile(REGEX_BREAK);

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), PATTERN_BREAK);
    }

    @Override
    public CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.BREAK_INCORRECT_SYNTAX;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        return "<br />";
    }
}
