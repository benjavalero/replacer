package es.bvalero.replacer.cosmetic;

import es.bvalero.replacer.finder.RegexIterable;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Find links where the alias matches with the target link and thus the alias can be removed,
 * e. g. `[[Madrid|Madrid]]`
 */
class SameLinkFinder implements CosmeticFinder {
    private static final String REGEX_SAME_LINK = "\\[\\[([^]|]+)\\|(\\1)]]";
    private static final Pattern PATTERN_SAME_LINK = Pattern.compile(REGEX_SAME_LINK, Pattern.CASE_INSENSITIVE);

    @Override
    public Iterable<Cosmetic> find(String text) {
        return new RegexIterable<Cosmetic>(text, PATTERN_SAME_LINK, this::convertMatch, this::isValidMatch);
    }

    private boolean isValidMatch(MatchResult matcher, String text) {
        String link = matcher.group(1);
        String title = matcher.group(2);
        return isSameLink(link, title);
    }

    private Cosmetic convertMatch(MatchResult match) {
        return Cosmetic.of(match.start(), match.group(), getFix(match));
    }

    private String getFix(MatchResult matcher) {
        String linkTitle = matcher.group(2);
        return String.format("[[%s]]", linkTitle);
    }

    private boolean isSameLink(String link, String title) {
        // Both parameters are equal ignoring the case
        return Character.isUpperCase(link.charAt(0)) || Character.isLowerCase(title.charAt(0));
    }
}
