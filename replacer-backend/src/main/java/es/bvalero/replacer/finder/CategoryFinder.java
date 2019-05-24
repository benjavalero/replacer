package es.bvalero.replacer.finder;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
class CategoryFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_CATEGORY = "\\[\\[(Categoría|als):[^]]+]]";
    private static final Pattern PATTERN_CATEGORY_TAG = Pattern.compile(REGEX_CATEGORY);

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        return findMatchResults(text, PATTERN_CATEGORY_TAG);
    }

}
