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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Find templates containing the useless "template" word */
@Slf4j
@Component
class TemplateWordFinder extends CosmeticCheckedFinder {

    @Resource
    private Map<String, String> templateWords;

    @RegExp
    private static final String REGEX_TEMPLATE_WORD = "\\{\\{(%s):(\\w.+?)}}";

    private Pattern patternTemplateWord;

    @PostConstruct
    public void init() {
        String alternate = StringUtils.join(templateWords.values(), "|");
        String regex = String.format(REGEX_TEMPLATE_WORD, alternate);
        patternTemplateWord = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), patternTemplateWord);
    }

    @Override
    protected CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.TEMPLATE_WORD_USELESS;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        return String.format("{{%s}}", match.group(2));
    }
}
