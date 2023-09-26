package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.intellij.lang.annotations.RegExp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Space links where the space is not translated, e.g. `[[File:x.jpg]] ==> [[Archivo:x.jpg]]` */
@Component
class SpaceNotTranslatedFinder implements CosmeticCheckedFinder {

    @RegExp
    private static final String REGEX_SPACE = "\\[\\[(%s):(.+?)]]";

    @Autowired
    private FinderProperties finderProperties;

    private Pattern patternLowercaseSpace;

    private final SetValuedMap<WikipediaLanguage, String> firstWords = new HashSetValuedHashMap<>();

    @PostConstruct
    public void init() {
        String concat = FinderUtils.joinAlternate(this.finderProperties.getAllSpaceWords());
        String regex = String.format(REGEX_SPACE, FinderUtils.toLowerCase(concat));
        this.patternLowercaseSpace = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            this.firstWords.put(lang, this.finderProperties.getFileWords().get(lang.getCode()).get(0));
            this.firstWords.put(lang, this.finderProperties.getImageWords().get(lang.getCode()).get(0));
            this.firstWords.put(lang, this.finderProperties.getAnnexWords().get(lang.getCode()).get(0));
            this.firstWords.put(lang, this.finderProperties.getCategoryWords().get(lang.getCode()).get(0));
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
        if (this.finderProperties.getAllFileWords().contains(spaceWord)) {
            spaceWordTranslated = this.finderProperties.getFileWords().get(lang).get(0);
        } else if (this.finderProperties.getAllImageWords().contains(spaceWord)) {
            spaceWordTranslated = this.finderProperties.getImageWords().get(lang).get(0);
        } else if (this.finderProperties.getAllAnnexWords().contains(spaceWord)) {
            spaceWordTranslated = this.finderProperties.getAnnexWords().get(lang).get(0);
        } else if (this.finderProperties.getAllCategoryWords().contains(spaceWord)) {
            spaceWordTranslated = this.finderProperties.getCategoryWords().get(lang).get(0);
        } else {
            throw new IllegalStateException("Unexpected value: " + spaceWord);
        }

        String spaceContent = match.group(2);
        return String.format("[[%s:%s]]", spaceWordTranslated, spaceContent);
    }
}
