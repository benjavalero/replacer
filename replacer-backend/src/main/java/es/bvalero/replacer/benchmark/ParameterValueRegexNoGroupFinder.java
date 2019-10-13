package es.bvalero.replacer.benchmark;

import es.bvalero.replacer.finder.IgnoredReplacement;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ParameterValueRegexNoGroupFinder extends ParameterValueAbstractFinder {

    private static final Pattern PATTERN
            = Pattern.compile(String.format("\\|\\s*(?:%s)\\s*=[^|}]+", StringUtils.join(PARAMS, "|")));

    Set<IgnoredReplacement> findMatches(String text) {
        Set<IgnoredReplacement> matches = new HashSet<>();
        Matcher m = PATTERN.matcher(text);
        while (m.find()) {
            int pos = m.group().indexOf("=") + 1;
            matches.add(IgnoredReplacement.of(m.start() + pos, m.group().substring(pos)));
        }
        return matches;
    }

}
