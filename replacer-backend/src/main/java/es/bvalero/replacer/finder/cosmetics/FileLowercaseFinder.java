package es.bvalero.replacer.finder.cosmetics;

import es.bvalero.replacer.finder.Cosmetic;
import es.bvalero.replacer.finder.CosmeticFinder;
import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.RegexIterable;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

@Component
public class FileLowercaseFinder implements CosmeticFinder {
    @Resource
    private List<String> fileSpaces;

    @RegExp
    private static final String REGEX_FILE_SPACE = "\\[\\[(%s)[:\\]]";

    @Override
    public Iterable<Cosmetic> find(String text) {
        String concat = fileSpaces.stream().map(String::toLowerCase).collect(Collectors.joining("|"));
        String regex = String.format(REGEX_FILE_SPACE, concat);
        WikipediaPage page = WikipediaPage.builder().content(text).lang(WikipediaLanguage.getDefault()).build();
        return new RegexIterable<>(page, Pattern.compile(regex), this::convert);
    }

    @Override
    public Cosmetic convert(MatchResult match) {
        return Cosmetic.of(match.start(1), match.group(1), getFix(match));
    }

    @Override
    public String getFix(MatchResult matcher) {
        return FinderUtils.setFirstUpperCase(matcher.group(1));
    }
}
