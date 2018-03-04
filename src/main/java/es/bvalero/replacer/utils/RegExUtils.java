package es.bvalero.replacer.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExUtils {

    private RegExUtils() {
    }

    /**
     * Finds the matches for a regular expression in a given text.
     */
    public static List<RegexMatch> findMatches(String text, String regex) {
        return findMatches(text, regex, 0);
    }

    /**
     * Finds the matches for a regular expression in a given text.
     */
    public static List<RegexMatch> findMatches(String text, String regex, int flags) {
        List<RegexMatch> matches = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex, flags);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            matches.add(new RegexMatch(matcher.start(), matcher.group(0)));
        }
        return matches;
    }

}
