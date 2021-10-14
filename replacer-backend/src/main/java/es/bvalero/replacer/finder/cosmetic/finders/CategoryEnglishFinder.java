package es.bvalero.replacer.finder.cosmetic.finders;

import static es.bvalero.replacer.finder.util.LinkUtils.START_LINK;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.util.LinkUtils;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.MatchResult;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Find categories in English */
@Slf4j
@Component
class CategoryEnglishFinder extends CosmeticCheckedFinder {

    private static final String CATEGORY_ENGLISH_WORD = "category";

    @Resource
    private Map<String, String> categoryWords;

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return new ArrayList<>(LinkUtils.findAllLinks(page));
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        String templateText = match.group();
        assert templateText.startsWith(START_LINK);
        int posColon = templateText.indexOf(':', START_LINK.length());
        if (posColon >= START_LINK.length()) {
            String templateName = templateText.substring(START_LINK.length(), posColon);
            return CATEGORY_ENGLISH_WORD.equalsIgnoreCase(templateName);
        }
        return false;
    }

    @Override
    protected CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.CATEGORY_IN_ENGLISH;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        String templateText = match.group();
        int posColon = templateText.indexOf(':', START_LINK.length());
        String categoryWord = categoryWords.get(page.getLang().getCode());
        return START_LINK + categoryWord + templateText.substring(posColon);
    }
}
