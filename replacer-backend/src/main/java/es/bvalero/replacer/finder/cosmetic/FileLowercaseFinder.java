package es.bvalero.replacer.finder.cosmetic;

import es.bvalero.replacer.finder.common.FinderPage;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

@Component
class FileLowercaseFinder implements CosmeticFinder {

    @RegExp
    private static final String REGEX_FILE_SPACE = "\\[\\[(%s)[:\\]]";

    @Resource
    private List<String> fileSpaces;

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        String concat = fileSpaces.stream().map(String::toLowerCase).collect(Collectors.joining("|"));
        String regex = String.format(REGEX_FILE_SPACE, concat);
        return RegexMatchFinder.find(page.getContent(), Pattern.compile(regex));
    }

    @Override
    public Cosmetic convert(MatchResult match) {
        return Cosmetic.builder().start(match.start(1)).text(match.group(1)).fix(getFix(match)).finder(this).build();
    }

    @Override
    public String getFix(MatchResult match) {
        return FinderUtils.setFirstUpperCase(match.group(1));
    }
}
