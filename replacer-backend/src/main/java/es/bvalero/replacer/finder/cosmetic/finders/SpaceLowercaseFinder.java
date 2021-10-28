package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticCheckedFinder;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Find space links where the space is in lowercase */
@Component
class SpaceLowercaseFinder extends CosmeticCheckedFinder {

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
        spaceWords.addAll(
            fileWords
                .values()
                .stream()
                .flatMap(val -> Arrays.stream(StringUtils.split(val)))
                .collect(Collectors.toList())
        );
        spaceWords.addAll(imageWords.values());
        spaceWords.addAll(annexWords.values());
        spaceWords.addAll(categoryWords.values());

        String concat = String.join("|", spaceWords);
        String regex = String.format(REGEX_SPACE, FinderUtils.toLowerCase(concat));
        patternLowercaseSpace = Pattern.compile(regex);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), patternLowercaseSpace);
    }

    @Override
    protected CheckWikipediaAction getCheckWikipediaAction() {
        // We return this action if the space fixed is not a Category
        return CheckWikipediaAction.CATEGORY_IN_LOWERCASE;
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        return String.format("[[%s:%s]]", FinderUtils.setFirstUpperCase(match.group(1)), match.group(2));
    }
}
