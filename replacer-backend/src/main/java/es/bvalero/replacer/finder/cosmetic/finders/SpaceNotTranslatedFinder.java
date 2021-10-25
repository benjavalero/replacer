package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Find space links where the space is not translated */
@Component
class SpaceNotTranslatedFinder extends CosmeticCheckedFinder {

    private static final String FILE_SPACE_EN = "File";
    private static final String IMAGE_SPACE_EN = "Image";
    private static final String ANNEX_SPACE_EN = "Annex";
    private static final String CATEGORY_SPACE_EN = "Category";

    @RegExp
    private static final String REGEX_SPACE = "\\[\\[(%s):(.+?)]]";

    @Resource
    private Map<String, String> fileWords;

    @Resource
    private Map<String, String> imageWords;

    @Resource
    private Map<String, String> annexWords;

    @Resource
    private Map<String, String> categoryWords;

    private Pattern patternLowercaseSpace;

    @PostConstruct
    public void init() {
        Set<String> spaceWords = Set.of(FILE_SPACE_EN, IMAGE_SPACE_EN, ANNEX_SPACE_EN, CATEGORY_SPACE_EN);
        String concat = String.join("|", spaceWords);
        String regex = String.format(REGEX_SPACE, FinderUtils.toLowerCase(concat));
        patternLowercaseSpace = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), patternLowercaseSpace);
    }

    @Override
    protected CheckWikipediaAction getCheckWikipediaAction() {
        // We return this action if the space fixed is not a Category
        return CheckWikipediaAction.CATEGORY_IN_ENGLISH;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        String spaceWord = FinderUtils.setFirstUpperCase(match.group(1));
        String spaceWordTranslated;
        switch (spaceWord) {
            case FILE_SPACE_EN:
                spaceWordTranslated = fileWords.get(page.getLang().getCode());
                break;
            case IMAGE_SPACE_EN:
                spaceWordTranslated = imageWords.get(page.getLang().getCode());
                break;
            case ANNEX_SPACE_EN:
                spaceWordTranslated = annexWords.get(page.getLang().getCode());
                break;
            case CATEGORY_SPACE_EN:
                spaceWordTranslated = categoryWords.get(page.getLang().getCode());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + spaceWord);
        }
        assert spaceWordTranslated != null;

        String spaceContent = match.group(2);
        return String.format("[[%s:%s]]", spaceWordTranslated, spaceContent);
    }
}
