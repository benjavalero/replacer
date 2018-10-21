package es.bvalero.replacer.article.exception;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.ArticleReplacementFinder;
import es.bvalero.replacer.article.IgnoredReplacementFinder;
import es.bvalero.replacer.persistence.ReplacementType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UrlFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_URL = "https?://<URI>";
    private static final RunAutomaton AUTOMATON_URL =
            new RunAutomaton(new RegExp(REGEX_URL).toAutomaton(new DatatypesAutomatonProvider()));

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_DOMAINS = "(<L>+\\.)+(com|org|es|net|gov|edu|gob|info)";
    private static final RunAutomaton AUTOMATON_DOMAIN =
            new RunAutomaton(new RegExp(REGEX_DOMAINS).toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<ArticleReplacement> findIgnoredReplacements(String text) {
        List<ArticleReplacement> matches = new ArrayList<>(100);
        matches.addAll(ArticleReplacementFinder.findReplacements(text, AUTOMATON_URL, ReplacementType.IGNORED));
        matches.addAll(ArticleReplacementFinder.findReplacements(text, AUTOMATON_DOMAIN, ReplacementType.IGNORED));
        return matches;
    }

}
