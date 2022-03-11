package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.common.domain.CheckWikipediaAction;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** External links with double HTTP, e.g. `https://https://www.linkedin.com ==> https://www.linkedin.com` */
@Component
class DoubleHttpFinder implements CosmeticCheckedFinder {

    @RegExp
    private static final String REGEX_DOUBLE_HTTP = "(https?://)\\1(\\S*)";

    private static final Pattern PATTERN_DOUBLE_HTTP = Pattern.compile(REGEX_DOUBLE_HTTP);

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return RegexMatchFinder.find(page.getContent(), PATTERN_DOUBLE_HTTP);
    }

    @Override
    public CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.EXTERNAL_LINK_WITH_DOUBLE_HTTP;
    }

    @Override
    public String getFix(MatchResult match, WikipediaPage page) {
        return match.group(1) + match.group(2);
    }
}
