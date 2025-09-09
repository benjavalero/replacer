package es.bvalero.replacer.finder.cosmetic.finders;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import es.bvalero.replacer.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Tags with no content, e.g. `<div style="text-align: right; font-size: 85%;"></div>` */
@Component
class TagEmptyFinder implements CosmeticFinder {

    private static final Set<String> TAGS = Set.of(
        "span",
        "div",
        "center",
        "gallery",
        "ref",
        "includeonly",
        "noinclude"
    );

    private static final Set<String> TAGS_SIMPLE = Set.of("ref");

    @SuppressWarnings("InlineFormatString")
    @RegExp
    private static final String REGEX_TAG_EMPTY = "<(%s)[^>]*></\\1>";

    private static final Pattern PATTERN_TAG_EMPTY = Pattern.compile(
        String.format(REGEX_TAG_EMPTY, FinderUtils.joinAlternate(TAGS))
    );

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), PATTERN_TAG_EMPTY);
    }

    @Override
    public boolean validate(MatchResult matchResult, FinderPage page) {
        final String tag = matchResult.group(1);
        if (TAGS_SIMPLE.contains(tag)) {
            final String expectedSimpleTag = String.format("<%s></%s>", tag, tag);
            return matchResult.group().equals(expectedSimpleTag);
        }
        return true;
    }

    @Override
    public CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.TAG_WITH_NO_CONTENT;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        return EMPTY;
    }
}
