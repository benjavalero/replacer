package es.bvalero.replacer.finder.benchmark;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ParameterValueRegexFinder extends ParameterValueAbstractFinder {

    private static final Pattern PATTERN
            = Pattern.compile(String.format("\\|\\s*(%s)\\s*=([^|}]+)", StringUtils.join(PARAMS, "|")));

    Set<IgnoredReplacement> findMatches(String text) {
        Set<IgnoredReplacement> matches = new HashSet<>();
        Matcher m = PATTERN.matcher(text);
        while (m.find()) {
            matches.add(IgnoredReplacement.of(m.start(2), m.group(2)));
        }
        return matches;
    }

}
