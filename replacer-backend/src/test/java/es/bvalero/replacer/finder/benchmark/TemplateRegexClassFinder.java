package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.MatchResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TemplateRegexClassFinder extends TemplateAbstractFinder {

    private static final String REGEX_TEMPLATE = "\\{\\{[^}]+?}}";
    private static final List<Pattern> PATTERNS = new ArrayList<>();

    TemplateRegexClassFinder(List<String> words) {
        for (String word : words) {
            if (startsWithLowerCase(word)) {
                PATTERNS.add(Pattern.compile(String.format("\\{\\{\\s*%s\\s*[|:](%s|[^}])+?}}", setFirstUpperCaseClass(word), REGEX_TEMPLATE)));
            } else {
                PATTERNS.add(Pattern.compile(String.format("\\{\\{\\s*%s\\s*[|:](%s|[^}])+?}}", word, REGEX_TEMPLATE)));
            }
        }
    }

    Set<MatchResult> findMatches(String text) {
        Set<MatchResult> matches = new HashSet<>();
        for (Pattern pattern : PATTERNS) {
            Matcher m = pattern.matcher(text);
            while (m.find()) {
                matches.add(MatchResult.of(m.start(), m.group()));
            }
        }
        return matches;
    }

}
