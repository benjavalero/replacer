package es.bvalero.replacer.finder.benchmark.completetag;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

class CompleteTagRegexAlternateFinder extends CompleteTagFinder {
    private Pattern pattern;

    CompleteTagRegexAlternateFinder(List<String> words) {
        String regex = String.format("<(%s).*?>.+?</\\1>", StringUtils.join(words, "|"));
        pattern = Pattern.compile(regex, Pattern.DOTALL);
    }

    Set<String> findMatches(String text) {
        Set<String> matches = new HashSet<>();
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            matches.add(m.group());
        }
        return matches;
    }
}
