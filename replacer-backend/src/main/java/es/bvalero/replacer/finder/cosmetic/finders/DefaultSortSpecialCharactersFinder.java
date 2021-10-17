package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Find template DEFAULTSORT including special characters */
@Component
class DefaultSortSpecialCharactersFinder extends CosmeticCheckedFinder {

    @RegExp
    private static final String REGEX_DEFAULTSORT_TEMPLATE = "\\{\\{\\s*DEFAULTSORT\\s*:(.+?)}}";

    private static final Pattern PATTERN_DEFAULTSORT_TEMPLATE = Pattern.compile(REGEX_DEFAULTSORT_TEMPLATE);

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), PATTERN_DEFAULTSORT_TEMPLATE);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return match.group(1).chars().anyMatch(this::isNotValidCharacter);
    }

    private boolean isNotValidCharacter(int ch) {
        return '_' == ch;
    }

    @Override
    protected CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.DEFAULT_SORT_SPECIAL_CHARACTERS;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        String templateContent = match.group(1).replace("_", " ").trim();
        return String.format("{{DEFAULTSORT:%s}}", templateContent);
    }
}
