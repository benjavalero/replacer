package es.bvalero.replacer.finder.cosmetic.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.SPACE;

import es.bvalero.replacer.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinder;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Unicode white-spaces, e.g. `\u2002` */
@Component
class UnicodeWhiteSpaceFinder implements CosmeticFinder {

    @RegExp
    private static final String REGEX_UNICODE_WHITE_SPACE = "\\p{Zs}";

    private static final Pattern PATTERN_UNICODE_WHITE_SPACE = Pattern.compile(REGEX_UNICODE_WHITE_SPACE);

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
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
    public CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.UNICODE_CONTROL_CHARACTERS;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        return SPACE;
    }
}
