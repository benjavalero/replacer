package es.bvalero.replacer.finder;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class PreFormatFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_PRE_FORMAT = "^ .+";
    private static final Pattern PATTERN_PRE_FORMAT = Pattern.compile(REGEX_PRE_FORMAT, Pattern.MULTILINE);

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        return findMatchResults(text, PATTERN_PRE_FORMAT);
    }

}
