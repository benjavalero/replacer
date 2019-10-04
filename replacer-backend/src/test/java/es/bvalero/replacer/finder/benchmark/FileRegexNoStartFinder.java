package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.IgnoredReplacement;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FileRegexNoStartFinder extends FileAbstractFinder {

    private static final Pattern PATTERN = Pattern.compile("(\\w[\\w. -]+\\.\\w{2,4}) *[]}|\n]", Pattern.UNICODE_CHARACTER_CLASS);

    Set<IgnoredReplacement> findMatches(String text) {
        Set<IgnoredReplacement> matches = new HashSet<>();
        Matcher m = PATTERN.matcher(text);
        while (m.find()) {
            matches.add(IgnoredReplacement.of(m.start(1), m.group(1)));
        }
        return matches;
    }

}
