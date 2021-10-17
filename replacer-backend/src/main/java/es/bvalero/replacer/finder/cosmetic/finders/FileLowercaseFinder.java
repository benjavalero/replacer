package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.finder.FinderPage;
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
    private static final String REGEX_FILE_SPACE = "\\[\\[(%s):(.+?)]]";

    @Resource
    private Set<String> fileSpaces;

    private Pattern patternFileSpace;

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
    public String getFix(MatchResult match, FinderPage page) {
        return String.format("[[%s:%s]]", FinderUtils.setFirstUpperCase(match.group(1)), match.group(2));
    }
}
