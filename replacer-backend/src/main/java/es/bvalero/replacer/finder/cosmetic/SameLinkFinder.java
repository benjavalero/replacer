package es.bvalero.replacer.finder.cosmetic;

import es.bvalero.replacer.finder.util.RegexMatchFinder;
import es.bvalero.replacer.page.IndexablePage;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

@Component
class SameLinkFinder extends CosmeticCheckedFinder {

    @RegExp
    private static final String REGEX_SAME_LINK = "\\[\\[([^]|]+)\\|(\\1)]]";

    private static final Pattern PATTERN_SAME_LINK = Pattern.compile(REGEX_SAME_LINK, Pattern.CASE_INSENSITIVE);

    @Override
    public Iterable<MatchResult> findMatchResults(IndexablePage page) {
        return RegexMatchFinder.find(page.getContent(), PATTERN_SAME_LINK);
    }

    @Override
    public boolean validate(MatchResult match, String text) {
        String link = match.group(1);
        String title = match.group(2);
        return isSameLink(link, title);
    }

    private boolean isSameLink(String link, String title) {
        // Both parameters are equal in case-sensitive
        // The first letter can be different if link is uppercase and the title is lowercase
        return (
            link.substring(1).equals(title.substring(1)) &&
            (Character.isLowerCase(title.charAt(0)) || Character.isUpperCase(link.charAt(0)))
        );
    }

    @Override
    int getFixId() {
        return 64;
    }

    @Override
    public String getFix(MatchResult match) {
        String linkTitle = match.group(2);
        return String.format("[[%s]]", linkTitle);
    }
}
