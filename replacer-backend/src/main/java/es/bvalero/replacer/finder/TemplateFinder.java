package es.bvalero.replacer.finder;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class TemplateFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    // The nested regex takes more but it is worth as it captures completely the templates with inner templates
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_TEMPLATE = "\\{\\{[^}]+?}}";
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_NESTED_TEMPLATE = "\\{\\{ *(%s)[ |\n]*[|:](%s|[^}])+?}}";
    private static final List<String> TEMPLATE_NAMES = Arrays.asList(
            "ORDENAR", "DEFAULTSORT", "NF", "TA", "commonscat", "coord",
            "cit[ae] ?<L>*", "quote", "cquote", "caja de cita");
    private static final RunAutomaton AUTOMATON_TEMPLATE;

    static {
        Set<String> wordsToJoin = new HashSet<>();
        for (String word : TEMPLATE_NAMES) {
            if (startsWithLowerCase(word)) {
                wordsToJoin.add(setFirstUpperCaseClass(word));
            } else {
                wordsToJoin.add(word);
            }
        }
        AUTOMATON_TEMPLATE = new RunAutomaton(new RegExp(
                String.format(REGEX_NESTED_TEMPLATE, StringUtils.join(wordsToJoin, "|"), REGEX_TEMPLATE))
                .toAutomaton(new DatatypesAutomatonProvider()));
    }

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        return findMatchResults(text, AUTOMATON_TEMPLATE);
    }

}
