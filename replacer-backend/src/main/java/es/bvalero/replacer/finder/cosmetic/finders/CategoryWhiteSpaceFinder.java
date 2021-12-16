package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaAction;
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

/** Find categories containing unnecessary spaces */
@Component
class CategoryWhiteSpaceFinder extends CosmeticCheckedFinder {

    @Resource
    private Map<String, String> categoryWords;

    @RegExp
    private static final String REGEX_CATEGORY_SPACE = "\\[\\[(\\s*%s\\s*):(.+?)(\\|.+?)?]]";

    private Pattern patternCategorySpace;

    @PostConstruct
    public void init() {
        Set<String> words = FinderUtils.getItemsInCollection(categoryWords.values());
        String alternate = String.format("(?:%s)", StringUtils.join(words, "|"));
        String regex = String.format(REGEX_CATEGORY_SPACE, alternate);
        patternCategorySpace = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), patternCategorySpace);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
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
        return !word.equals(word.trim());
    }

    private boolean validateCategoryName(String name) {
        return !name.equals(name.trim());
    }

    private boolean validateCategoryAlias(@Nullable String alias) {
        // We trim also the alias except if it is an empty whitespace which has a special meaning
        assert alias == null || alias.startsWith("|");
        if (alias == null || StringUtils.isBlank(alias.substring(1))) {
            return false;
        } else {
            return !alias.substring(1).equals(alias.substring(1).trim());
        }
    }

    @Override
    protected CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.CATEGORY_WITH_WHITESPACE;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        String categoryWord = FinderUtils.getFirstItemInList(categoryWords.get(page.getLang().getCode()));
        return String.format(
            "[[%s:%s%s]]",
            categoryWord,
            match.group(2).trim(),
            match.group(3) == null ? "" : "|" + match.group(3).substring(1).trim()
        );
    }
}
