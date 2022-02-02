package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Unicode white-spaces, e.g. `\u2002` */
@Component
class UnicodeWhiteSpaceFinder extends CosmeticCheckedFinder {

    private static final String COMMON_WHITE_SPACE = "\u0020";

    @RegExp
    private static final String REGEX_UNICODE_WHITE_SPACE = "\\p{Zs}";

    private static final Pattern PATTERN_UNICODE_WHITE_SPACE = Pattern.compile(REGEX_UNICODE_WHITE_SPACE);

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), PATTERN_UNICODE_WHITE_SPACE);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        String matchText = match.group();
        assert matchText.length() == 1;
        int code = matchText.charAt(0);
        return 8192 <= code && code <= 8202;
    }

    @Override
    protected CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.UNICODE_CONTROL_CHARACTERS;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        return COMMON_WHITE_SPACE;
    }
}
