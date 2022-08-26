package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.common.domain.CheckWikipediaAction;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/** Categories containing unnecessary spaces, e.g. `[[Categoría: Animal]] ==> [[Categoría:Animal]]` */
@Component
class CategoryWhiteSpaceFinder implements CosmeticCheckedFinder {

    @Resource
    private Map<String, String> categoryWords;

    @RegExp
    private static final String REGEX_CATEGORY_SPACE = "\\[\\[(\\s*%s\\s*):(.+?)(\\|.+?)?]]";

    private Pattern patternCategorySpace;

    @PostConstruct
    public void init() {
        Set<String> words = FinderUtils.getItemsInCollection(categoryWords.values());
        String alternate = String.format("(?:%s)", FinderUtils.joinAlternate(words));
        String regex = String.format(REGEX_CATEGORY_SPACE, alternate);
        patternCategorySpace = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return RegexMatchFinder.find(page.getContent(), patternCategorySpace);
    }

    @Override
    public boolean validate(MatchResult match, WikipediaPage page) {
        String categoryWord = match.group(1);
        String categoryName = match.group(2);
        String categoryAlias = match.group(3);
        return (
            validateCategoryWord(categoryWord) ||
            validateCategoryName(categoryName) ||
            validateCategoryAlias(categoryAlias)
        );
    }

    private boolean validateCategoryWord(String word) {
        return hasSpacesAround(word);
    }

    private boolean validateCategoryName(String name) {
        return hasSpacesAround(name);
    }

    private boolean validateCategoryAlias(@Nullable String alias) {
        // We trim also the alias except if it is an empty whitespace which has a special meaning
        if (alias == null) {
            return false;
        } else {
            assert alias.startsWith("|");
            final String aliasValue = alias.substring(1);
            if (isEmptyAlias(aliasValue)) {
                return false;
            } else {
                return hasSpacesAround(aliasValue);
            }
        }
    }

    private boolean hasSpacesAround(String text) {
        return !text.equals(text.trim());
    }

    private boolean isEmptyAlias(String alias) {
        return StringUtils.isBlank(alias);
    }

    @Override
    public CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.CATEGORY_WITH_WHITESPACE;
    }

    @Override
    public String getFix(MatchResult match, WikipediaPage page) {
        String defaultCategoryWord = FinderUtils.getFirstItemInList(
            categoryWords.get(page.getId().getLang().getCode())
        );
        String fixedCategoryName = match.group(2).trim();
        String fixedCategoryAlias = match.group(3) == null ? "" : "|" + match.group(3).substring(1).trim();
        return String.format("[[%s:%s%s]]", defaultCategoryWord, fixedCategoryName, fixedCategoryAlias);
    }
}
