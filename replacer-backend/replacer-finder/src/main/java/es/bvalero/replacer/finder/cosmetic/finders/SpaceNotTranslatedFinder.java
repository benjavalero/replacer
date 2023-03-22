package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
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

/** Space links where the space is not translated, e.g. `[[File:x.jpg]] ==> [[Archivo:x.jpg]]` */
@Component
class SpaceNotTranslatedFinder implements CosmeticCheckedFinder {

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
        spaceWords.addAll(FinderUtils.getItemsInCollection(this.fileWords.values()));
        spaceWords.addAll(FinderUtils.getItemsInCollection(this.imageWords.values()));
        spaceWords.addAll(FinderUtils.getItemsInCollection(this.annexWords.values()));
        spaceWords.addAll(FinderUtils.getItemsInCollection(this.categoryWords.values()));

        String concat = FinderUtils.joinAlternate(spaceWords);
        String regex = String.format(REGEX_SPACE, FinderUtils.toLowerCase(concat));
        this.patternLowercaseSpace = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            this.firstWords.put(lang, FinderUtils.getFirstItemInList(this.fileWords.get(lang.getCode())));
            this.firstWords.put(lang, FinderUtils.getFirstItemInList(this.imageWords.get(lang.getCode())));
            this.firstWords.put(lang, FinderUtils.getFirstItemInList(this.annexWords.get(lang.getCode())));
            this.firstWords.put(lang, FinderUtils.getFirstItemInList(this.categoryWords.get(lang.getCode())));
        }
    }

    @Override
    public FinderPriority getPriority() {
        // To have a little more priority than the space-lowercase finder
        return FinderPriority.LOW;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), this.patternLowercaseSpace);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        String spaceWord = FinderUtils.setFirstUpperCase(match.group(1));
        return !this.firstWords.get(page.getPageKey().getLang()).contains(spaceWord);
    }

    @Override
    public CheckWikipediaAction getCheckWikipediaAction() {
        // We return this action if the space fixed is not a Category
        return CheckWikipediaAction.CATEGORY_IN_ENGLISH;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        String spaceWord = FinderUtils.toFirstUpperCase(match.group(1));
        String spaceWordTranslated;
        String lang = page.getPageKey().getLang().getCode();
        if (FinderUtils.getItemsInCollection(this.fileWords.values()).contains(spaceWord)) {
            spaceWordTranslated = FinderUtils.getFirstItemInList(this.fileWords.get(lang));
        } else if (FinderUtils.getItemsInCollection(this.imageWords.values()).contains(spaceWord)) {
            spaceWordTranslated = FinderUtils.getFirstItemInList(this.imageWords.get(lang));
        } else if (FinderUtils.getItemsInCollection(this.annexWords.values()).contains(spaceWord)) {
            spaceWordTranslated = FinderUtils.getFirstItemInList(this.annexWords.get(lang));
        } else if (FinderUtils.getItemsInCollection(this.categoryWords.values()).contains(spaceWord)) {
            spaceWordTranslated = FinderUtils.getFirstItemInList(this.categoryWords.get(lang));
        } else {
            throw new IllegalStateException("Unexpected value: " + spaceWord);
        }

        String spaceContent = match.group(2);
        return String.format("[[%s:%s]]", spaceWordTranslated, spaceContent);
    }
}
