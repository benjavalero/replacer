package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.MatchResult;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FileRegexNoStartLazyFinder extends FileAbstractFinder {

    private final static Pattern PATTERN = Pattern.compile("(\\w[\\w. -]+?\\.\\w{2,4}) *[]}|\n]", Pattern.UNICODE_CHARACTER_CLASS);

    Set<MatchResult> findMatches(String text) {
        Set<MatchResult> matches = new HashSet<>();
        Matcher m = PATTERN.matcher(text);
        while (m.find()) {
            matches.add(new MatchResult(m.start(1), m.group(1)));
        }
        return matches;
    }

}
