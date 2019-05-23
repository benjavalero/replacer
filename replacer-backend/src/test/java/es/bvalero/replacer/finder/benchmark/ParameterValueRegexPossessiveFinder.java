package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.MatchResult;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ParameterValueRegexPossessiveFinder extends ParameterValueAbstractFinder {

    private final static Pattern PATTERN
            = Pattern.compile(String.format("\\|\\s*(%s)\\s*=([^|}]++)", StringUtils.join(PARAMS, "|")));

    Set<MatchResult> findMatches(String text) {
        Set<MatchResult> matches = new HashSet<>();
        Matcher m = PATTERN.matcher(text);
        while (m.find()) {
            matches.add(new MatchResult(m.start(2), m.group(2)));
        }
        return matches;
    }

}
