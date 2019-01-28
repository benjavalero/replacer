package es.bvalero.replacer.finder.ignored;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.ReplacementFinder;
import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import es.bvalero.replacer.persistence.ReplacementType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CompleteTemplateFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    // The nested regex takes twice more but it is worth as it captures completely the templates with inner templates
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_TEMPLATE = "\\{\\{[^}]+}}";

    // The template NF usually involves ORDENAR so it is normal that the names and surnames have no diacritics
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_TEMPLATE_NAMES =
            "(ORDENAR:|DEFAULTSORT:|NF\\||[Cc]ita\\||c?[Qq]uote\\||[Cc]oord\\||[Cc]ommonscat\\|)";

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_COMPLETE_TEMPLATE =
            "\\{\\{" + REGEX_TEMPLATE_NAMES + '(' + REGEX_TEMPLATE + "|[^}])+}}";
    private static final RunAutomaton AUTOMATON_COMPLETE_TEMPLATE =
            new RunAutomaton(new RegExp(REGEX_COMPLETE_TEMPLATE).toAutomaton());

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_CATEGORY = "\\[\\[(Categoría|als):[^]]+]]";
    private static final RunAutomaton AUTOMATON_CATEGORY =
            new RunAutomaton(new RegExp(REGEX_CATEGORY).toAutomaton());

    @Override
    public List<ArticleReplacement> findIgnoredReplacements(String text) {
        List<ArticleReplacement> matches = new ArrayList<>(100);
        matches.addAll(findReplacements(text, AUTOMATON_COMPLETE_TEMPLATE, ReplacementType.IGNORED));
        matches.addAll(findReplacements(text, AUTOMATON_CATEGORY, ReplacementType.IGNORED));
        return matches;
    }

}
