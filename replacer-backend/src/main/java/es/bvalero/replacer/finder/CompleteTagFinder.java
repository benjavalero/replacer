package es.bvalero.replacer.finder;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class CompleteTagFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    private static final List<String> TAG_NAMES =
            Arrays.asList("math", "source", "syntaxhighlight", "blockquote", "pre", "score", "poem", "ref", "code", "cite");
    private static final Collection<Pattern> PATTERN_COMPLETE_TAGS = new ArrayList<>(TAG_NAMES.size());

    static {
        TAG_NAMES.forEach(word -> PATTERN_COMPLETE_TAGS.add(Pattern.compile(String.format("<%s.*?>.+?</%s>", word, word), Pattern.DOTALL)));
    }

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        List<MatchResult> matches = new ArrayList<>(100);
        for (Pattern pattern : PATTERN_COMPLETE_TAGS) {
            matches.addAll(findMatchResults(text, pattern));
        }
        return matches;
    }

}
