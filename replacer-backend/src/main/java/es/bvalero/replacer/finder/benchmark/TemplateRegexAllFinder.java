package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.FinderUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TemplateRegexAllFinder extends TemplateAbstractFinder {

    private static final String REGEX_TEMPLATE = "\\{\\{[^}]+?}}";
    private static Pattern pattern;

    TemplateRegexAllFinder(List<String> words) {
        Set<String> wordsToJoin = new HashSet<>();
        for (String word : words) {
            wordsToJoin.add(word);
            if (FinderUtils.startsWithLowerCase(word)) {
                wordsToJoin.add(FinderUtils.setFirstUpperCase(word));
            }
        }
        pattern = Pattern.compile(String.format("\\{\\{\\s*(%s)\\s*[|:](%s|[^}])+?}}", StringUtils.join(wordsToJoin, "|"), REGEX_TEMPLATE));
    }

    Set<FinderResult> findMatches(String text) {
        Set<FinderResult> matches = new HashSet<>();
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            matches.add(FinderResult.of(m.start(), m.group()));
        }
        return matches;
    }

}
