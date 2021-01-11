package es.bvalero.replacer.finder.cosmetics;

import es.bvalero.replacer.finder.Cosmetic;
import es.bvalero.replacer.finder.CosmeticFinder;
import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.page.IndexablePage;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/**
 * Find links where the alias matches with the target link and thus the alias can be removed,
 * e.g. `[[Madrid|Madrid]]`
 */
@Component
class SameLinkFinder implements CosmeticFinder {

    @RegExp
    private static final String REGEX_SAME_LINK = "\\[\\[([^]|]+)\\|(\\1)]]";

    private static final Pattern PATTERN_SAME_LINK = Pattern.compile(REGEX_SAME_LINK, Pattern.CASE_INSENSITIVE);

    @Override
    public Iterable<Cosmetic> find(String text) {
        WikipediaPage page = WikipediaPage.builder().content(text).lang(WikipediaLanguage.getDefault()).build();
        return new RegexIterable<>(page, PATTERN_SAME_LINK, this::convert, this::isValidMatch);
    }

    private boolean isValidMatch(MatchResult matcher, IndexablePage page) {
        String link = matcher.group(1);
        String title = matcher.group(2);
        return isSameLink(link, title);
    }

    @Override
    public String getFix(MatchResult matcher) {
        String linkTitle = matcher.group(2);
        return String.format("[[%s]]", linkTitle);
    }

    @Override
    public Optional<Integer> getFixId() {
        return Optional.of(64);
    }

    private boolean isSameLink(String link, String title) {
        // Both parameters are equal in case-sensitive
        // The first letter can be different if link is uppercase and the title is lowercase
        return (
            link.substring(1).equals(title.substring(1)) &&
            (Character.isLowerCase(title.charAt(0)) || Character.isUpperCase(link.charAt(0)))
        );
    }
}
