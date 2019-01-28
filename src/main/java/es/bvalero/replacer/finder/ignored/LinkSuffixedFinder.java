package es.bvalero.replacer.finder.ignored;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.ReplacementFinder;
import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import es.bvalero.replacer.persistence.ReplacementType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LinkSuffixedFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_LINK_SUFFIXED = "(\\[\\[|\\|)<L>+]]<Ll>+";
    private static final RunAutomaton AUTOMATON_LINK_SUFFIXED =
            new RunAutomaton(new RegExp(REGEX_LINK_SUFFIXED).toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<ArticleReplacement> findIgnoredReplacements(String text) {
        return findReplacements(text, AUTOMATON_LINK_SUFFIXED, ReplacementType.IGNORED);
    }

}
