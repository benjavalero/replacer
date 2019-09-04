package es.bvalero.replacer.cosmetic;

import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.ArticleReplacementFinder;
import es.bvalero.replacer.finder.ReplacementSuggestion;
import org.intellij.lang.annotations.RegExp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SameLinkFinder implements ArticleReplacementFinder {

    @RegExp
    private static final String REGEX_SAME_LINK = "\\[\\[([^]|]+)\\|(\\1)]]";
    private static final Pattern PATTERN_SAME_LINK = Pattern.compile(REGEX_SAME_LINK, Pattern.CASE_INSENSITIVE);

    @Override
    public List<ArticleReplacement> findReplacements(String text) {
        List<ArticleReplacement> replacements = new ArrayList<>();
        Matcher matcher = PATTERN_SAME_LINK.matcher(text);
        while (matcher.find()) {
            String link = matcher.group(1);
            String title = matcher.group(2);
            if (isSameLink(link, title)) {
                replacements.add(ArticleReplacement.builder()
                        .start(matcher.start())
                        .text(matcher.group())
                        .suggestions(Collections.singletonList(
                                ReplacementSuggestion.ofNoComment(String.format("[[%s]]", title))))
                        .build());
            }
        }
        return replacements;
    }

    private boolean isSameLink(String link, String title) {
        return (link.substring(1).equalsIgnoreCase(title.substring(1))) &&
                (Character.isUpperCase(link.charAt(0)) || Character.isLowerCase(title.charAt(0)));
    }

    @Override
    public String getType() {
        return null;
    }

}
