package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TemplateFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    // The nested regex takes more but it is worth as it captures completely the templates with inner templates
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_TEMPLATE = "\\{\\{[^}]+?}}";
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_NESTED_TEMPLATE = "\\{\\{(%s)[|:](%s|[^}])+?}}";
    private static final List<String> TEMPLATE_NAMES =
            Arrays.asList("cita", "quote", "cquote", "caja de cita", "coord", "commonscat", "ORDENAR", "DEFAULTSORT", "NF");
    private static final RunAutomaton AUTOMATON_TEMPLATE;

    static {
        Set<String> wordsToJoin = new HashSet<>();
        for (String word : TEMPLATE_NAMES) {
            wordsToJoin.add(word);
            if (isLowercase(word)) {
                wordsToJoin.add(setFirstUpperCase(word));
            }
        }
        AUTOMATON_TEMPLATE = new RunAutomaton(new RegExp(String.format(REGEX_NESTED_TEMPLATE, StringUtils.join(wordsToJoin, "|"), REGEX_TEMPLATE)).toAutomaton());
    }

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        List<MatchResult> matches = new ArrayList<>(100);
        matches.addAll(findMatchResults(text, AUTOMATON_TEMPLATE));
        return matches;
    }

}
