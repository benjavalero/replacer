package es.bvalero.replacer.article.exception;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.ArticleReplacementFinder;
import es.bvalero.replacer.article.IgnoredReplacementFinder;
import es.bvalero.replacer.persistence.ReplacementType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class CompleteTagFinder implements IgnoredReplacementFinder {

    // The gain on performance is so high (500x faster) using the text-directed version that it is better
    // to make different regex for each tag name
    private static final String[] TAG_NAMES = {"math", "source", "syntaxhighlight", "blockquote"};

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
    public List<ArticleReplacement> findIgnoredReplacements(String text) {
        List<ArticleReplacement> matches = new ArrayList<>(100);
        for (RunAutomaton automaton : AUTOMATON_COMPLETE_TAGS) {
            matches.addAll(ArticleReplacementFinder.findReplacements(text, automaton, ReplacementType.IGNORED));
        }
        return matches;
    }

}
