package es.bvalero.replacer.finder.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.IgnoredReplacement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class TemplateAutomatonFinder extends TemplateAbstractFinder {

    private static final String REGEX_TEMPLATE = "\\{\\{[^}]+}}";
    private static final String REGEX_TEMPLATE_COMPLETE = "\\{\\{ *%s[ |\n]*[|:](%s|[^}])+?}}";
    private static final List<RunAutomaton> AUTOMATA = new ArrayList<>();

    TemplateAutomatonFinder(List<String> words) {
        for (String word : words) {
            AUTOMATA.add(new RunAutomaton(new RegExp(
                    String.format(REGEX_TEMPLATE_COMPLETE, word, REGEX_TEMPLATE)).toAutomaton()));
            if (startsWithLowerCase(word)) {
                AUTOMATA.add(new RunAutomaton(new RegExp(
                        String.format(REGEX_TEMPLATE_COMPLETE, setFirstUpperCase(word), REGEX_TEMPLATE)).toAutomaton()));
            }
        }
    }

    Set<IgnoredReplacement> findMatches(String text) {
        Set<IgnoredReplacement> matches = new HashSet<>();
        for (RunAutomaton automaton : AUTOMATA) {
            AutomatonMatcher m = automaton.newMatcher(text);
            while (m.find()) {
                matches.add(IgnoredReplacement.of(m.start(), m.group()));
            }
        }
        return matches;
    }

}
