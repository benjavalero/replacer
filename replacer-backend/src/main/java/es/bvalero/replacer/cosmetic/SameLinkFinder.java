package es.bvalero.replacer.cosmetic;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinder;
import es.bvalero.replacer.finder.ReplacementSuggestion;
import org.intellij.lang.annotations.RegExp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SameLinkFinder implements ReplacementFinder {

    @RegExp
    private static final String REGEX_SAME_LINK = "\\[\\[([^]|]+)\\|(\\1)]]";
    private static final Pattern PATTERN_SAME_LINK = Pattern.compile(REGEX_SAME_LINK, Pattern.CASE_INSENSITIVE);

    @Override
    public List<Replacement> findReplacements(String text) {
        List<Replacement> replacements = new ArrayList<>(100);
        Matcher matcher = PATTERN_SAME_LINK.matcher(text);
        while (matcher.find()) {
            String link = matcher.group(1);
            String title = matcher.group(2);
            if (isSameLink(link, title)) {
                replacements.add(convertMatch(matcher.start(), matcher.group(), title));
            }
        }
        return replacements;
    }

    public Replacement convertMatch(int start, String text, String linkTitle) {
        return Replacement.builder()
                .type(getType())
                .subtype(getSubtype())
                .start(start)
                .text(text)
                .suggestions(findSuggestions(linkTitle))
                .build();
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getSubtype() {
        return null;
    }

    @Override
    public List<ReplacementSuggestion> findSuggestions(String linkTitle) {
        String fixedLink = String.format("[[%s]]", linkTitle);
        return Collections.singletonList(ReplacementSuggestion.ofNoComment(fixedLink));
    }

    private boolean isSameLink(String link, String title) {
        return (link.substring(1).equalsIgnoreCase(title.substring(1))) &&
                (Character.isUpperCase(link.charAt(0)) || Character.isLowerCase(title.charAt(0)));
    }

}
