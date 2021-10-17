package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Find categories in English */
@Component
class CategoryEnglishFinder extends CosmeticCheckedFinder {

    @RegExp
    private static final String REGEX_CATEGORY_ENGLISH = "\\[\\[\\s*Category\\s*:(.+?)]]";

    private static final Pattern PATTERN_CATEGORY_ENGLISH = Pattern.compile(
        REGEX_CATEGORY_ENGLISH,
        Pattern.CASE_INSENSITIVE
    );

    @Resource
    private Map<String, String> categoryWords;

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), PATTERN_CATEGORY_ENGLISH);
    }

    @Override
    protected CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.CATEGORY_IN_ENGLISH;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        String categoryWord = categoryWords.get(page.getLang().getCode());
        String templateContent = match.group(1).replace("_", " ").trim();
        return String.format("[[%s:%s]]", categoryWord, templateContent);
    }
}
