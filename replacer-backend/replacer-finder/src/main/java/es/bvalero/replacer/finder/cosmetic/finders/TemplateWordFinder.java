package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Templates containing the useless "template" word, e.g. `{{plantilla:DGRG}} ==> {{DGRG}}` */
@Component
class TemplateWordFinder implements CosmeticCheckedFinder {

    @Resource
    private Map<String, String> templateWords;

    @RegExp
    private static final String REGEX_TEMPLATE_WORD = "\\{\\{(%s):(\\w.+?)}}";

    private Pattern patternTemplateWord;

    @PostConstruct
    public void init() {
        String alternate = FinderUtils.joinAlternate(this.templateWords.values());
        String regex = String.format(REGEX_TEMPLATE_WORD, alternate);
        this.patternTemplateWord = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), this.patternTemplateWord);
    }

    @Override
    public CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.TEMPLATE_WORD_USELESS;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        return String.format("{{%s}}", match.group(2));
    }
}
