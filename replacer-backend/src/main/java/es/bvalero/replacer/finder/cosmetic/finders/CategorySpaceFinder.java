package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Find categories containing unnecessary spaces */
@Component
class CategorySpaceFinder extends CosmeticCheckedFinder {

    @Resource
    private Map<String, String> categoryWords;

    @RegExp
    private static final String REGEX_CATEGORY_SPACE = "\\[\\[(\\s*%s\\s*):(.+?)]]";

    private Pattern patternCategorySpace;

    @PostConstruct
    public void init() {
        String alternate = String.format("(?:%s)", StringUtils.join(categoryWords.values(), "|"));
        String regex = String.format(REGEX_CATEGORY_SPACE, alternate);
        patternCategorySpace = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), patternCategorySpace);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        String templateName = match.group(1);
        String templateContent = match.group(2);
        return !templateName.equals(templateName.trim()) || !templateContent.equals(templateContent.trim());
    }

    @Override
    protected CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.CATEGORY_WITH_WHITESPACE;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        String categoryWord = categoryWords.get(page.getLang().getCode());
        return String.format("[[%s:%s]]", categoryWord, match.group(2).trim());
    }
}
