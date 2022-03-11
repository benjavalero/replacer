package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.common.domain.CheckWikipediaAction;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Break with incorrect syntax, e.g. `</br> ==> <br />` */
@Component
class BreakIncorrectFinder implements CosmeticCheckedFinder {

    static final String BREAK_XHTML = "<br />";
    private static final String BREAK_XHTML_NO_SPACE = "<br/>";
    private static final String BREAK_HTML5 = "<br>";
    private static final Set<String> BREAK_VALID = Set.of(BREAK_XHTML, BREAK_XHTML_NO_SPACE, BREAK_HTML5);

    @RegExp
    private static final String REGEX_BREAK = "<[/\\s\\\\]*br[/\\s\\\\.]*>";

    private static final Pattern PATTERN_BREAK = Pattern.compile(REGEX_BREAK);

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), PATTERN_BREAK);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return !BREAK_VALID.contains(match.group());
    }

    @Override
    public CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.BREAK_INCORRECT_SYNTAX;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        // See https://es.wikipedia.org/wiki/Usuario:Benjavalero/Replacer/br
        return BREAK_XHTML;
    }
}
