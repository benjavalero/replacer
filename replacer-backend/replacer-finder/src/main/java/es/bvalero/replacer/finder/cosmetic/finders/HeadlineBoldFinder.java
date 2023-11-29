package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinder;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Headlines with the complete text in bold, e.g. `== '''Asia''' ==` */
@Component
class HeadlineBoldFinder implements CosmeticFinder {

    @RegExp
    private static final String REGEX_HEADLINE_BOLD = "^(={2,})\\s*'''([^=']+?)'''\\s*\\1$";

    private static final Pattern PATTERN_HEADLINE_BOLD = Pattern.compile(REGEX_HEADLINE_BOLD, Pattern.MULTILINE);

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), PATTERN_HEADLINE_BOLD);
    }

    @Override
    public CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.HEADLINE_BOLD;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        return String.format("%s %s %s", match.group(1), match.group(2), match.group(1));
    }
}
