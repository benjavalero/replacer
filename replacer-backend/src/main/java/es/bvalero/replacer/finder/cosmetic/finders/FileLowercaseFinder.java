package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.Cosmetic;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

@Component
class FileLowercaseFinder implements CosmeticFinder {

    @RegExp
    private static final String REGEX_FILE_SPACE = "\\[\\[(%s)[:\\]]";

    private Pattern patternFileSpace;

    @Resource
    private List<String> fileSpaces;

    @PostConstruct
    public void init() {
        String concat = fileSpaces.stream().map(String::toLowerCase).collect(Collectors.joining("|"));
        String regex = String.format(REGEX_FILE_SPACE, concat);
        patternFileSpace = Pattern.compile(regex);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), patternFileSpace);
    }

    @Override
    public Cosmetic convert(MatchResult match) {
        return Cosmetic.builder().start(match.start(1)).text(match.group(1)).fix(getFix(match)).build();
    }

    @Override
    public String getFix(MatchResult match) {
        return FinderUtils.setFirstUpperCase(match.group(1));
    }
}
