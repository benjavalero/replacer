package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.common.domain.CheckWikipediaAction;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Tags with no content, e.g. `<div style="text-align: right; font-size: 85%;"></div>` */
@Component
class TagEmptyFinder implements CosmeticCheckedFinder {

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

    @RegExp
    private static final String REGEX_TAG_EMPTY = "<(%s)[^>]*></\\1>";

    private static final Pattern PATTERN_TAG_EMPTY = Pattern.compile(
        String.format(REGEX_TAG_EMPTY, StringUtils.join(TAGS, "|"))
    );

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return RegexMatchFinder.find(page.getContent(), PATTERN_TAG_EMPTY);
    }

    @Override
    public boolean validate(MatchResult matchResult, WikipediaPage page) {
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
    public String getFix(MatchResult match, WikipediaPage page) {
        return "";
    }
}
