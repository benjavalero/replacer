package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Unnecessary small tag in sup or ref tags, e.g. `<sup><small>2</small></sup> ==> <sup>2</sup>` */
@Component
class SmallTagUnnecessaryFinder implements CosmeticFinder {

    @RegExp
    private static final String REGEX_SUP_SMALL_TAG = "<sup><small>.+?</small></sup>";

    @RegExp
    private static final String REGEX_SMALL_REF_TAG = "<small><ref.+?</ref></small>";

    @RegExp
    private static final String REGEX_REF_SMALL_TAG = "<ref.+?><small>.+?</small></ref>";

    private static final Pattern PATTERN_SMALL_TAG_UNNECESSARY = Pattern.compile(
        FinderUtils.joinAlternate(List.of(REGEX_SUP_SMALL_TAG, REGEX_SMALL_REF_TAG, REGEX_REF_SMALL_TAG))
    );

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), PATTERN_SMALL_TAG_UNNECESSARY);
    }

    @Override
    public CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.SMALL_TAG_UNNECESSARY;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        return match.group().replaceAll("</?small>", "");
    }
}
