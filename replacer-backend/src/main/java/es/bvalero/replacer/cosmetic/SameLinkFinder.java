package es.bvalero.replacer.cosmetic;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinder;
import es.bvalero.replacer.finder.Suggestion;
import org.intellij.lang.annotations.RegExp;

import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

class SameLinkFinder implements ReplacementFinder {

    @RegExp
    private static final String REGEX_SAME_LINK = "\\[\\[([^]|]+)\\|(\\1)]]";
    private static final Pattern PATTERN_SAME_LINK = Pattern.compile(REGEX_SAME_LINK, Pattern.CASE_INSENSITIVE);

    @Override
    public List<Replacement> findReplacements(String text) {
        return findMatchResults(text, PATTERN_SAME_LINK);
    }

    @Override
    public boolean isValidMatch(MatchResult matcher, String text) {
        String link = matcher.group(1);
        String title = matcher.group(2);
        return isSameLink(link, title);
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getSubtype(String text) {
        return null;
    }

    @Override
    public List<Suggestion> findSuggestions(MatchResult matcher) {
        String linkTitle = matcher.group(2);
        String fixedLink = String.format("[[%s]]", linkTitle);
        return Collections.singletonList(Suggestion.ofNoComment(fixedLink));
    }

    private boolean isSameLink(String link, String title) {
        return (link.substring(1).equalsIgnoreCase(title.substring(1))) &&
                (Character.isUpperCase(link.charAt(0)) || Character.isLowerCase(title.charAt(0)));
    }

    @Override
    public List<Suggestion> findSuggestions(String text) {
        return Collections.emptyList();
    }

}
