package es.bvalero.replacer.finder;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class CompleteTagFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    private static final List<String> TAG_NAMES = Arrays.asList(
            "nowiki", "pre", "code", "source", "syntaxhighlight",
            "math", "poem", "score",
            "ref", "blockquote", "cite");
    private static final List<Pattern> PATTERN_COMPLETE_TAGS = TAG_NAMES.stream()
            .map(word -> Pattern.compile(String.format("<%s[^>/]*>.+?</%s>", word, word), Pattern.DOTALL))
            .collect(Collectors.toList());

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        return findMatchResultsFromPatterns(text, PATTERN_COMPLETE_TAGS);
    }

}
