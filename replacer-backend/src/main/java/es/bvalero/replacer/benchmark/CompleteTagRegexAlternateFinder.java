package es.bvalero.replacer.benchmark;

import es.bvalero.replacer.finder.IgnoredReplacement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

class CompleteTagRegexAlternateFinder extends CompleteTagAbstractFinder {
    private Pattern pattern;

    CompleteTagRegexAlternateFinder(List<String> words) {
        String regex = String.format("<(%s).*?>.+?</\\1>", StringUtils.join(words, "|"));
        pattern = Pattern.compile(regex, Pattern.DOTALL);
    }

    Set<IgnoredReplacement> findMatches(String text) {
        Set<IgnoredReplacement> matches = new HashSet<>();
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            matches.add(IgnoredReplacement.of(m.start(), m.group()));
        }
        return matches;
    }
}
