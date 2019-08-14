package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.MatchResult;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FileRegexLazyFinder extends FileAbstractFinder {

    private static final Pattern PATTERN = Pattern.compile("[:=|] *([^=|]+?\\.\\w{2,4}) *[]}|\n]");

    Set<MatchResult> findMatches(String text) {
        Set<MatchResult> matches = new HashSet<>();
        Matcher m = PATTERN.matcher(text);
        while (m.find()) {
            matches.add(MatchResult.of(m.start(1), m.group(1)));
        }
        return matches;
    }

}
