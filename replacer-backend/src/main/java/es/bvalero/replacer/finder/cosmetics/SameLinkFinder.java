package es.bvalero.replacer.finder.cosmetics;

import es.bvalero.replacer.finder.Cosmetic;
import es.bvalero.replacer.finder.CosmeticFinder;
import es.bvalero.replacer.finder.RegexIterable;
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
        return new RegexIterable<>(text, PATTERN_SAME_LINK, this::convert, this::isValidMatch);
    }

    private boolean isValidMatch(MatchResult matcher, String text) {
        String link = matcher.group(1);
        String title = matcher.group(2);
        return isSameLink(link, title);
    }

    @Override
    public String getFix(MatchResult matcher) {
        String linkTitle = matcher.group(2);
        return String.format("[[%s]]", linkTitle);
    }

    private boolean isSameLink(String link, String title) {
        // Both parameters are equal ignoring the case
        return Character.isUpperCase(link.charAt(0)) || Character.isLowerCase(title.charAt(0));
    }
}
