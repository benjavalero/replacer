package es.bvalero.replacer.finder.cosmetic.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.PIPE;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import jakarta.annotation.PostConstruct;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/** Categories containing unnecessary spaces, e.g. `[[Categoría: Animal]] ==> [[Categoría:Animal]]` */
@Component
class CategoryWhiteSpaceFinder implements CosmeticFinder {

    @SuppressWarnings("InlineFormatString")
    @RegExp
    private static final String REGEX_CATEGORY_SPACE = "\\[\\[(\\s*%s\\s*):(.+?)(\\|.+?)?]]";

    // Dependency injection
    private final FinderProperties finderProperties;

    private Pattern patternCategorySpace;

    CategoryWhiteSpaceFinder(FinderProperties finderProperties) {
        this.finderProperties = finderProperties;
    }

    @PostConstruct
    public void init() {
        String alternate = String.format(
            "(?:%s)",
            FinderUtils.joinAlternate(this.finderProperties.getAllCategoryWords())
        );
        String regex = String.format(REGEX_CATEGORY_SPACE, alternate);
        this.patternCategorySpace = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), this.patternCategorySpace);
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
    public String getFix(MatchResult match, FinderPage page) {
        // We take profit and fix also the category space if not translated
        String defaultCategoryWord =
            this.finderProperties.getCategoryWords().get(page.getPageKey().getLang().getCode()).getFirst();
        String fixedCategoryName = match.group(2).trim();
        String fixedCategoryAlias = match.group(3) == null ? "" : PIPE + match.group(3).substring(1).trim();
        return String.format("[[%s:%s%s]]", defaultCategoryWord, fixedCategoryName, fixedCategoryAlias);
    }
}
