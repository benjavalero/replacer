package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.MatchResult;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TemplateRegexClassAllFinder extends TemplateAbstractFinder {

    private static final String REGEX_TEMPLATE = "\\{\\{[^}]+?}}";
    private static Pattern pattern;

    TemplateRegexClassAllFinder(List<String> words) {
        Set<String> wordsToJoin = new HashSet<>();
        for (String word : words) {
            if (startsWithLowerCase(word)) {
                wordsToJoin.add(setFirstUpperCaseClass(word));
            } else {
                wordsToJoin.add(word);
            }
        }
        pattern = Pattern.compile(String.format("\\{\\{\\s*(%s)\\s*[|:](%s|[^}])+?}}", StringUtils.join(wordsToJoin, "|"), REGEX_TEMPLATE));
    }

    Set<MatchResult> findMatches(String text) {
        Set<MatchResult> matches = new HashSet<>();
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            matches.add(MatchResult.of(m.start(), m.group()));
        }
        return matches;
    }

}
