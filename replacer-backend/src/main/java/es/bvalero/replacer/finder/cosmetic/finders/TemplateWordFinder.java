package es.bvalero.replacer.finder.cosmetic.finders;

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
        assert templateText.startsWith(TemplateUtils.START_TEMPLATE);
        int posColon = templateText.indexOf(':', TemplateUtils.START_TEMPLATE.length());
        if (posColon >= TemplateUtils.START_TEMPLATE.length()) {
            String langTemplateWord = templateWords.get(page.getLang().getCode());
            String templateWord = templateText.substring(2, posColon).trim();
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
        int posColon = templateText.indexOf(':', TemplateUtils.START_TEMPLATE.length());
        assert posColon >= TemplateUtils.START_TEMPLATE.length();
        assert templateText.endsWith(TemplateUtils.END_TEMPLATE);
        String afterColon = templateText.substring(posColon + 1).trim();
        if (afterColon.startsWith(TemplateUtils.START_TEMPLATE)) {
            String innerTemplate = afterColon.substring(0, afterColon.length() - TemplateUtils.END_TEMPLATE.length());
            if (innerTemplate.endsWith(TemplateUtils.END_TEMPLATE)) {
                return innerTemplate;
            } else {
                LOGGER.error("Unsupported template word case: {}", templateText);
                return templateText;
            }
        } else {
            return TemplateUtils.START_TEMPLATE + afterColon;
        }
    }
}
