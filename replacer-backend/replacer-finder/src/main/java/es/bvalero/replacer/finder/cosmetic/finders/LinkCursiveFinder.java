package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Links with the same link and alias, e.g. `[[Coronavirus|coronavirus]] ==> [[coronavirus]]` */
@Component
class LinkCursiveFinder implements CosmeticFinder {

    @RegExp
    private static final String REGEX_SAME_LINK = "\\[\\[([^|\\[\\]]+\\|)?''([^|\\[\\]]+)'']]";

    private static final Pattern PATTERN_SAME_LINK = Pattern.compile(REGEX_SAME_LINK, Pattern.CASE_INSENSITIVE);

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), PATTERN_SAME_LINK);
    }

    @Override
    public CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.LINK_EQUAL_TO_LINK_TEXT;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        String linkTitle = Objects.requireNonNullElse(match.group(1), StringUtils.EMPTY);
        String linkAlias = match.group(2);
        return String.format("''[[%s%s]]''", linkTitle, linkAlias);
    }
}
