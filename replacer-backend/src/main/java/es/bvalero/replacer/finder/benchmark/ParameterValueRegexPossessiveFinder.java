package es.bvalero.replacer.finder.benchmark;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

class ParameterValueRegexPossessiveFinder extends ParameterValueAbstractFinder {
    private static final Pattern PATTERN = Pattern.compile(
        String.format("\\|\\s*(%s)\\s*=([^|}]++)", StringUtils.join(PARAMS, "|"))
    );

    Set<FinderResult> findMatches(String text) {
        Set<FinderResult> matches = new HashSet<>();
        Matcher m = PATTERN.matcher(text);
        while (m.find()) {
            matches.add(FinderResult.of(m.start(2), m.group(2)));
        }
        return matches;
    }
}
