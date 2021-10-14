package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.Cosmetic;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Find file links where the file space is in lowercase */
@Component
class FileLowercaseFinder implements CosmeticFinder {

    @RegExp
    private static final String REGEX_FILE_SPACE = "\\[\\[(%s)[:\\]]";

    private Pattern patternFileSpace;

    @Resource
    private Set<String> fileSpaces;

    @PostConstruct
    public void init() {
        String concat = String.join("|", fileSpaces);
        String regex = String.format(REGEX_FILE_SPACE, concat);
        patternFileSpace = Pattern.compile(regex);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), patternFileSpace);
    }

    @Override
    public Cosmetic convert(MatchResult match, FinderPage page) {
        return Cosmetic.builder().start(match.start(1)).text(match.group(1)).fix(getFix(match, page)).build();
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        return FinderUtils.setFirstUpperCase(match.group(1));
    }
}
