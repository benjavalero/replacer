package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.immutable.finders.TemplateFinder;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.MatchResult;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Find templates names containing unnecessarily the "template" word */
@Slf4j
@Component
class TemplateWordFinder extends CosmeticCheckedFinder {

    @Resource
    private Map<String, String> templateWords;

    @Autowired
    private TemplateFinder templateFinder;

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return new ArrayList<>(templateFinder.findAllTemplates(page));
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        String templateText = match.group();
        assert templateText.startsWith(TemplateFinder.START_TEMPLATE);
        int posColon = templateText.indexOf(':', TemplateFinder.START_TEMPLATE.length());
        if (posColon >= TemplateFinder.START_TEMPLATE.length()) {
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
        int posColon = templateText.indexOf(':', TemplateFinder.START_TEMPLATE.length());
        assert posColon >= TemplateFinder.START_TEMPLATE.length();
        assert templateText.endsWith(TemplateFinder.END_TEMPLATE);
        String afterColon = templateText.substring(posColon + 1).trim();
        if (afterColon.startsWith(TemplateFinder.START_TEMPLATE)) {
            String innerTemplate = afterColon.substring(0, afterColon.length() - TemplateFinder.END_TEMPLATE.length());
            if (innerTemplate.endsWith(TemplateFinder.END_TEMPLATE)) {
                return innerTemplate;
            } else {
                LOGGER.error("Unsupported template word case: {}", templateText);
                return templateText;
            }
        } else {
            return TemplateFinder.START_TEMPLATE + afterColon;
        }
    }
}
