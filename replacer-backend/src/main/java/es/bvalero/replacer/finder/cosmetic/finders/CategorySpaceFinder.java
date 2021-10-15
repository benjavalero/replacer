package es.bvalero.replacer.finder.cosmetic.finders;

import static es.bvalero.replacer.finder.util.LinkUtils.END_LINK;
import static es.bvalero.replacer.finder.util.LinkUtils.START_LINK;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinkUtils;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.MatchResult;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/** Find categories in English */
@Component
class CategorySpaceFinder extends CosmeticCheckedFinder {

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
            String cleanTemplateName = FinderUtils.setFirstUpperCase(templateName.trim());
            if (categoryWords.containsValue(cleanTemplateName)) {
                // At this point we can assure the link is a Category
                // Check if there are additional spaces around the template name
                if (!templateName.equalsIgnoreCase(cleanTemplateName)) {
                    return true;
                }

                // Check if there are additional spaces around the category text
                String categoryText = templateText.substring(posColon + 1);
                assert categoryText.endsWith(END_LINK);
                categoryText = categoryText.substring(0, categoryText.length() - END_LINK.length());
                return !categoryText.equals(categoryText.trim());
            }
        }
        return false;
    }

    @Override
    protected CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.CATEGORY_WITH_WHITESPACE;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        String templateText = match.group();
        int posColon = templateText.indexOf(':', START_LINK.length());
        String categoryWord = categoryWords.get(page.getLang().getCode());
        String categoryText = templateText.substring(posColon + 1, templateText.length() - END_LINK.length()).trim();
        return START_LINK + categoryWord + ":" + categoryText + END_LINK;
    }
}
