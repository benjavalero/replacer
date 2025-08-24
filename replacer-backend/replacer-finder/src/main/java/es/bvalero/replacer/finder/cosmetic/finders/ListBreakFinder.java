package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinder;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** List items ending with a break, e.g. `* x <br> ==> * x` */
@Component
class ListBreakFinder implements CosmeticFinder {

    @RegExp
    private static final String REGEX_LIST_BREAK = "^(\\*.+?)<br ?/?>$";

    private static final Pattern PATTERN_LIST_BREAK = Pattern.compile(REGEX_LIST_BREAK, Pattern.MULTILINE);

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
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
