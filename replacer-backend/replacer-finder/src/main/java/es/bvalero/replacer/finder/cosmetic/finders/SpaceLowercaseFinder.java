package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.FinderPage;
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
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Space links where the space is in lowercase, e.g. `[[archivo:x.jpg]] ==> [[Archivo:x.jpg]]` */
@Component
class SpaceLowercaseFinder implements CosmeticCheckedFinder {

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
        Set<String> spaceWords = new HashSet<>();
        spaceWords.addAll(FinderUtils.getItemsInCollection(this.fileWords.values()));
        spaceWords.addAll(FinderUtils.getItemsInCollection(this.imageWords.values()));
        spaceWords.addAll(FinderUtils.getItemsInCollection(this.annexWords.values()));
        spaceWords.addAll(FinderUtils.getItemsInCollection(this.categoryWords.values()));

        String concat = FinderUtils.joinAlternate(spaceWords);
        String regex = String.format(REGEX_SPACE, FinderUtils.toLowerCase(concat));
        this.patternLowercaseSpace = Pattern.compile(regex);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), this.patternLowercaseSpace);
    }

    @Override
    public CheckWikipediaAction getCheckWikipediaAction() {
        // We return this action if the space fixed is not a Category
        return CheckWikipediaAction.CATEGORY_IN_LOWERCASE;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        return String.format("[[%s:%s]]", FinderUtils.setFirstUpperCase(match.group(1)), match.group(2));
    }
}
