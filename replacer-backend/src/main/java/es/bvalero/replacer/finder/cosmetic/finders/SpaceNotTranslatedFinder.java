package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Find space links where the space is not translated */
@Component
class SpaceNotTranslatedFinder extends CosmeticCheckedFinder {

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

    private final SetValuedMap<WikipediaLanguage, String> firstWords = new HashSetValuedHashMap<>();

    @PostConstruct
    public void init() {
        Set<String> spaceWords = new HashSet<>();
        spaceWords.addAll(FinderUtils.getItemsInCollection(fileWords.values()));
        spaceWords.addAll(FinderUtils.getItemsInCollection(imageWords.values()));
        spaceWords.addAll(FinderUtils.getItemsInCollection(annexWords.values()));
        spaceWords.addAll(FinderUtils.getItemsInCollection(categoryWords.values()));

        String concat = String.join("|", spaceWords);
        String regex = String.format(REGEX_SPACE, FinderUtils.toLowerCase(concat));
        patternLowercaseSpace = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            this.firstWords.put(lang, FinderUtils.getFirstItemInList(fileWords.get(lang.getCode())));
            this.firstWords.put(lang, FinderUtils.getFirstItemInList(imageWords.get(lang.getCode())));
            this.firstWords.put(lang, FinderUtils.getFirstItemInList(annexWords.get(lang.getCode())));
            this.firstWords.put(lang, FinderUtils.getFirstItemInList(categoryWords.get(lang.getCode())));
        }
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), patternLowercaseSpace);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        String spaceWord = FinderUtils.setFirstUpperCase(match.group(1));
        return !this.firstWords.get(page.getLang()).contains(spaceWord);
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
        if (FinderUtils.getItemsInCollection(fileWords.values()).contains(spaceWord)) {
            spaceWordTranslated = FinderUtils.getFirstItemInList(fileWords.get(page.getLang().getCode()));
        } else if (FinderUtils.getItemsInCollection(imageWords.values()).contains(spaceWord)) {
            spaceWordTranslated = FinderUtils.getFirstItemInList(imageWords.get(page.getLang().getCode()));
        } else if (FinderUtils.getItemsInCollection(annexWords.values()).contains(spaceWord)) {
            spaceWordTranslated = FinderUtils.getFirstItemInList(annexWords.get(page.getLang().getCode()));
        } else if (FinderUtils.getItemsInCollection(categoryWords.values()).contains(spaceWord)) {
            spaceWordTranslated = FinderUtils.getFirstItemInList(categoryWords.get(page.getLang().getCode()));
        } else {
            throw new IllegalStateException("Unexpected value: " + spaceWord);
        }

        String spaceContent = match.group(2);
        return String.format("[[%s:%s]]", spaceWordTranslated, spaceContent);
    }
}
