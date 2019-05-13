package es.bvalero.replacer.finder.ignored;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import es.bvalero.replacer.finder.MatchResult;
import es.bvalero.replacer.finder.ReplacementFinder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class CompleteTagFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    // The gain on performance is so high (500x faster) using the text-directed version that it is better
    // to make different regex for each tag name
    private static final String[] TAG_NAMES = {"math", "source", "syntaxhighlight", "blockquote", "pre", "score", "poem"};

    private static final Collection<RunAutomaton> AUTOMATON_COMPLETE_TAGS = new ArrayList<>(TAG_NAMES.length);

    static {
        for (String tagName : TAG_NAMES) {
            // Build an alternation for the negation of the closing tag
            Collection<String> alternatives = new ArrayList<>(tagName.length() + 1);
            alternatives.add("\\<[^/]");
            for (int i = 0; i < tagName.length(); i++) {
                alternatives.add("\\</" + tagName.substring(0, i) + "[^" + tagName.substring(i, i + 1) + ']');
            }

            String regex = "\\<" + tagName + "([^\\<]|" + StringUtils.join(alternatives, "|") + ")+\\</" + tagName + "\\>";
            RunAutomaton automaton = new RunAutomaton(new RegExp(regex).toAutomaton());
            AUTOMATON_COMPLETE_TAGS.add(automaton);
        }
    }

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        List<MatchResult> matches = new ArrayList<>(100);
        for (RunAutomaton automaton : AUTOMATON_COMPLETE_TAGS) {
            matches.addAll(findMatchResults(text, automaton));
        }
        return matches;
    }

}
