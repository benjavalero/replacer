package es.bvalero.replacer.finder.benchmark;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

class CompleteTagRegexAlternateNegatedFinder extends CompleteTagAbstractFinder {
    private Pattern pattern;

    CompleteTagRegexAlternateNegatedFinder(List<String> words) {
        String regex = String.format("<(%s)[^>]*?>.+?</\\1>", StringUtils.join(words, "|"));
        pattern = Pattern.compile(regex, Pattern.DOTALL);
    }

    Set<FinderResult> findMatches(String text) {
        Set<FinderResult> matches = new HashSet<>();
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            matches.add(FinderResult.of(m.start(), m.group()));
        }
        return matches;
    }
}
