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

/** Headlines ending with a colon, e.g. `== Asia: ==` */
@Component
class HeadlineColonFinder implements CosmeticFinder {

    @RegExp
    private static final String REGEX_HEADLINE_COLON = "^(={2,5})\\s*([^\\s=:]+?):\\s*\\1$";

    private static final Pattern PATTERN_HEADLINE_COLON = Pattern.compile(REGEX_HEADLINE_COLON, Pattern.MULTILINE);

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), PATTERN_HEADLINE_COLON);
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
