package es.bvalero.replacer.finder.cosmetic.finders;

import static es.bvalero.replacer.finder.util.TemplateUtils.END_TEMPLATE;
import static es.bvalero.replacer.finder.util.TemplateUtils.START_TEMPLATE;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.util.TemplateUtils;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.MatchResult;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Find templates names containing unnecessarily the "template" word */
@Slf4j
@Component
class TemplateWordFinder extends CosmeticCheckedFinder {

    @Resource
    private Map<String, String> templateWords;

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return new ArrayList<>(TemplateUtils.findAllTemplates(page));
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        String templateText = match.group();
        assert templateText.startsWith(START_TEMPLATE);
        int posColon = templateText.indexOf(':', START_TEMPLATE.length());
        if (posColon >= START_TEMPLATE.length()) {
            String langTemplateWord = templateWords.get(page.getLang().getCode());
            String templateWord = templateText.substring(START_TEMPLATE.length(), posColon).trim();
            return langTemplateWord.equalsIgnoreCase(templateWord);
        }
        return false;
    }

    @Override
    protected CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.TEMPLATE_CONTAINS_USELESS_WORD_TEMPLATE;
    }

    @Override
    public String getFix(MatchResult match) {
        String templateText = match.group();
        int posColon = templateText.indexOf(':', START_TEMPLATE.length());
        assert posColon >= START_TEMPLATE.length();
        assert templateText.endsWith(END_TEMPLATE);
        String afterColon = templateText.substring(posColon + 1).trim();
        if (afterColon.startsWith(START_TEMPLATE)) {
            String innerTemplate = afterColon.substring(0, afterColon.length() - END_TEMPLATE.length());
            if (innerTemplate.endsWith(END_TEMPLATE)) {
                return innerTemplate;
            } else {
                LOGGER.error("Unsupported template word case: {}", templateText);
                return templateText;
            }
        } else {
            return START_TEMPLATE + afterColon;
        }
    }
}
