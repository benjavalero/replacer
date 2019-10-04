package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.IgnoredReplacement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TemplateRegexFinder extends TemplateAbstractFinder {

    private static final String REGEX_TEMPLATE = "\\{\\{[^}]+?}}";
    private static final List<Pattern> PATTERNS = new ArrayList<>();

    TemplateRegexFinder(List<String> words) {
        for (String word : words) {
            PATTERNS.add(Pattern.compile(String.format("\\{\\{\\s*%s\\s*[|:](%s|[^}])+?}}", word, REGEX_TEMPLATE)));
            if (startsWithLowerCase(word)) {
                PATTERNS.add(Pattern.compile(String.format("\\{\\{\\s*%s\\s*[|:](%s|[^}])+?}}", setFirstUpperCase(word), REGEX_TEMPLATE)));
            }
        }
    }

    Set<IgnoredReplacement> findMatches(String text) {
        Set<IgnoredReplacement> matches = new HashSet<>();
        for (Pattern pattern : PATTERNS) {
            Matcher m = pattern.matcher(text);
            while (m.find()) {
                matches.add(IgnoredReplacement.of(m.start(), m.group()));
            }
        }
        return matches;
    }

}
