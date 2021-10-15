package es.bvalero.replacer.finder.cosmetic.finders;

import static es.bvalero.replacer.finder.util.TemplateUtils.END_TEMPLATE;
import static es.bvalero.replacer.finder.util.TemplateUtils.START_TEMPLATE;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.util.TemplateUtils;
import java.util.ArrayList;
import java.util.regex.MatchResult;
import org.springframework.stereotype.Component;

/** Find template DEFAULTSORT including special characters */
@Component
class DefaultSortSpecialCharactersFinder extends CosmeticCheckedFinder {

    private static final String DEFAULT_SORT_TEMPLATE = "DEFAULTSORT";

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
            String templateName = templateText.substring(START_TEMPLATE.length(), posColon).trim();
            if (DEFAULT_SORT_TEMPLATE.equals(templateName)) {
                String templateContent = templateText.substring(posColon + 1);
                assert templateContent.endsWith(END_TEMPLATE);
                return templateContent.chars().anyMatch(this::isNotValidCharacter);
            }
        }
        return false;
    }

    private boolean isNotValidCharacter(int ch) {
        return '_' == ch;
    }

    @Override
    protected CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.DEFAULT_SORT_SPECIAL_CHARACTERS;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        return match.group().replaceAll("_", " ");
    }
}
