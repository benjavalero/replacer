package es.bvalero.replacer.finder.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class TemplateAutomatonAllFinder extends TemplateAbstractFinder {

    private static final String REGEX_TEMPLATE = "\\{\\{[^}]+?}}";
    private static final String REGEX_TEMPLATE_COMPLETE = "\\{\\{ *(%s)[ |\n]*[|:](%s|[^}])+?}}";
    private static RunAutomaton automaton;

    TemplateAutomatonAllFinder(List<String> words) {
        Set<String> wordsToJoin = new HashSet<>();
        for (String word : words) {
            wordsToJoin.add(word);
            if (FinderUtils.startsWithLowerCase(word)) {
                wordsToJoin.add(FinderUtils.setFirstUpperCase(word));
            }
        }
        automaton = new RunAutomaton(new RegExp(
                String.format(REGEX_TEMPLATE_COMPLETE, StringUtils.join(wordsToJoin, "|"), REGEX_TEMPLATE))
                .toAutomaton());
    }

    Set<FinderResult> findMatches(String text) {
        Set<FinderResult> matches = new HashSet<>();
        AutomatonMatcher m = automaton.newMatcher(text);
        while (m.find()) {
            matches.add(FinderResult.of(m.start(), m.group()));
        }
        return matches;
    }

}
