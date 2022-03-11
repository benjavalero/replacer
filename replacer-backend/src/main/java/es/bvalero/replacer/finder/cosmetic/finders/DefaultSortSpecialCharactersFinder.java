package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.common.domain.CheckWikipediaAction;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/**
 * Template DEFAULTSORT including special characters, e.g. `{{ DEFAULTSORT : AES_Andes_2 }} ==> {{DEFAULTSORT:AES Andes 2}}`
 * We also find and fix cases with a whitespace after the colon though they are not reported (action 88).
 */
@Component
class DefaultSortSpecialCharactersFinder implements CosmeticCheckedFinder {

    private static final List<String> SORT_TEMPLATES = List.of("DEFAULTSORT", "ORDENAR");

    @RegExp
    private static final String REGEX_DEFAULTSORT_TEMPLATE = "\\{\\{(\\s*%s\\s*):(.+?)}}";

    private static final Pattern PATTERN_DEFAULTSORT_TEMPLATE = Pattern.compile(
        String.format(REGEX_DEFAULTSORT_TEMPLATE, String.format("(?:%s)", StringUtils.join(SORT_TEMPLATES, "|")))
    );

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), PATTERN_DEFAULTSORT_TEMPLATE);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return match.group(2).chars().anyMatch(this::isNotValidCharacter);
    }

    private boolean isNotValidCharacter(int ch) {
        return '_' == ch;
    }

    @Override
    public CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.DEFAULT_SORT_SPECIAL_CHARACTERS;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        String templateName = match.group(1).trim();
        String templateContent = match.group(2).replace("_", " ").trim();
        return String.format("{{%s:%s}}", templateName, templateContent);
    }
}
