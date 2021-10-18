package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinder;
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
class SpaceNotTranslatedFinder implements CosmeticFinder {

    private static final String FILE_SPACE_EN = "File";
    private static final String IMAGE_SPACE_EN = "Image";
    private static final String ANNEX_SPACE_EN = "Annex";

    @RegExp
    private static final String REGEX_SPACE = "\\[\\[(%s):(.+?)]]";

    @Resource
    private Map<String, String> fileWords;

    @Resource
    private Map<String, String> imageWords;

    @Resource
    private Map<String, String> annexWords;

    private Pattern patternLowercaseSpace;

    @PostConstruct
    public void init() {
        Set<String> spaceWords = Set.of(FILE_SPACE_EN, IMAGE_SPACE_EN, ANNEX_SPACE_EN);
        String concat = String.join("|", spaceWords);
        String regex = String.format(REGEX_SPACE, FinderUtils.toLowerCase(concat));
        patternLowercaseSpace = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), patternLowercaseSpace);
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        String spaceWord = FinderUtils.setFirstUpperCase(match.group(1));
        String spaceWordTranslated = null;
        if (FILE_SPACE_EN.equals(spaceWord)) {
            spaceWordTranslated = fileWords.get(page.getLang().getCode());
        } else if (IMAGE_SPACE_EN.equals(spaceWord)) {
            spaceWordTranslated = imageWords.get(page.getLang().getCode());
        }
        if (ANNEX_SPACE_EN.equals(spaceWord)) {
            spaceWordTranslated = annexWords.get(page.getLang().getCode());
        }
        assert spaceWordTranslated != null;

        String spaceContent = match.group(2);
        return String.format("[[%s:%s]]", spaceWordTranslated, spaceContent);
    }
}
