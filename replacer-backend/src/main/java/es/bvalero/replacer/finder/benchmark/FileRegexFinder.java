package es.bvalero.replacer.finder.benchmark;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FileRegexFinder extends FileAbstractFinder {
    private static final Pattern PATTERN = Pattern.compile("[:=|] *([^]=|\n]+\\.\\w{2,4}) *[]}|\n]");

    Set<FinderResult> findMatches(String text) {
        Set<FinderResult> matches = new HashSet<>();
        Matcher m = PATTERN.matcher(text);
        while (m.find()) {
            matches.add(FinderResult.of(m.start(1), m.group(1)));
        }
        return matches;
    }
}
